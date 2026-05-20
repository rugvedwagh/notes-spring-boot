package com.rugved.notes.controller;

import com.rugved.notes.config.JwtService;
import com.rugved.notes.model.User;
import com.rugved.notes.repository.NoteRepository;
import com.rugved.notes.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class NoteControllerTest {

    @Autowired MockMvc mvc;
    @Autowired NoteRepository noteRepo;
    @Autowired UserRepository userRepo;
    @Autowired JwtService jwtService;
    @Autowired PasswordEncoder passwordEncoder;

    private String token;
    private User user;

    @BeforeEach
    void setUp() {
        noteRepo.deleteAll();
        userRepo.deleteAll();

        user = new User();
        user.setEmail("test@test.com");
        user.setPassword(passwordEncoder.encode("123456"));
        userRepo.save(user);

        token = "Bearer " + jwtService.generateToken("test@test.com");
    }

    @Test
    void createNote_returnsCreated() throws Exception {
        mvc.perform(post("/notes")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"My Note\",\"content\":\"Hello\",\"tags\":[\"work\"]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("My Note"))
                .andExpect(jsonPath("$.tags[0]").value("work"));
    }

    @Test
    void createNote_failsWithEmptyTitle() throws Exception {
        mvc.perform(post("/notes")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"content\":\"Hello\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Title cannot be empty"));
    }

    @Test
    void getAllNotes_returnsPaginatedNotes() throws Exception {
        mvc.perform(post("/notes").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Note 1\",\"content\":\"Content 1\"}"));
        mvc.perform(post("/notes").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Note 2\",\"content\":\"Content 2\"}"));

        mvc.perform(get("/notes").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getNoteById_returnsNote() throws Exception {
        String response = mvc.perform(post("/notes")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Find me\",\"content\":\"Here\"}"))
                .andReturn().getResponse().getContentAsString();

        Long id = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(response).get("id").asLong();

        mvc.perform(get("/notes/" + id).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Find me"));
    }

    @Test
    void updateNote_updatesSuccessfully() throws Exception {
        String response = mvc.perform(post("/notes")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Old title\",\"content\":\"Old content\"}"))
                .andReturn().getResponse().getContentAsString();

        Long id = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(response).get("id").asLong();

        mvc.perform(put("/notes/" + id)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New title\",\"content\":\"New content\",\"tags\":[\"updated\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New title"))
                .andExpect(jsonPath("$.tags[0]").value("updated"));
    }

    @Test
    void deleteNote_deletesSuccessfully() throws Exception {
        String response = mvc.perform(post("/notes")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Delete me\",\"content\":\"bye\"}"))
                .andReturn().getResponse().getContentAsString();

        Long id = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(response).get("id").asLong();

        mvc.perform(delete("/notes/" + id).header("Authorization", token))
                .andExpect(status().isNoContent());

        mvc.perform(get("/notes/" + id).header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchByTitle_returnsMatchingNotes() throws Exception {
        mvc.perform(post("/notes").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Spring Boot Guide\",\"content\":\"content\"}"));
        mvc.perform(post("/notes").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Java Tips\",\"content\":\"content\"}"));

        mvc.perform(get("/notes/search?title=spring").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Spring Boot Guide"));
    }

    @Test
    void getByTag_returnsNotesWithTag() throws Exception {
        mvc.perform(post("/notes").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Note 1\",\"content\":\"content\",\"tags\":[\"work\"]}"));
        mvc.perform(post("/notes").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Note 2\",\"content\":\"content\",\"tags\":[\"personal\"]}"));

        mvc.perform(get("/notes/tag/work").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Note 1"));
    }

    @Test
    void cannotAccessOtherUsersNote() throws Exception {
        // create note as user 1
        String response = mvc.perform(post("/notes")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Private note\",\"content\":\"secret\"}"))
                .andReturn().getResponse().getContentAsString();

        Long id = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(response).get("id").asLong();

        // create user 2 and get their token
        User user2 = new User();
        user2.setEmail("other@test.com");
        user2.setPassword(passwordEncoder.encode("123456"));
        userRepo.save(user2);
        String token2 = "Bearer " + jwtService.generateToken("other@test.com");

        // user 2 tries to access user 1's note
        mvc.perform(get("/notes/" + id).header("Authorization", token2))
                .andExpect(status().isNotFound());
    }

    @Test
    void protectedRoute_failsWithoutToken() throws Exception {
        mvc.perform(get("/notes"))
                .andExpect(status().isForbidden());
    }
}