package com.rugved.notes.repository;

import com.rugved.notes.model.Note;
import com.rugved.notes.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class NoteRepositoryTest {

    @Autowired NoteRepository noteRepo;
    @Autowired UserRepository userRepo;

    private User user;
    private User otherUser;

    @BeforeEach
    void setUp() {
        noteRepo.deleteAll();
        userRepo.deleteAll();

        user = new User();
        user.setEmail("test@test.com");
        user.setPassword("password");
        userRepo.save(user);

        otherUser = new User();
        otherUser.setEmail("other@test.com");
        otherUser.setPassword("password");
        userRepo.save(otherUser);
    }

    private Note createNote(String title, String content, List<String> tags, User owner) {
        Note note = new Note();
        note.setTitle(title);
        note.setContent(content);
        note.setTags(tags);
        note.setUser(owner);
        return noteRepo.save(note);
    }

    @Test
    void findByUser_returnsOnlyUserNotes() {
        createNote("My Note", "content", List.of(), user);
        createNote("Other Note", "content", List.of(), otherUser);

        Page<Note> result = noteRepo.findByUser(user, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("My Note");
    }

    @Test
    void findByIdAndUser_returnsNote_whenBelongsToUser() {
        Note saved = createNote("My Note", "content", List.of(), user);

        Optional<Note> result = noteRepo.findByIdAndUser(saved.getId(), user);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("My Note");
    }

    @Test
    void findByIdAndUser_returnsEmpty_whenBelongsToOtherUser() {
        Note saved = createNote("Other Note", "content", List.of(), otherUser);

        Optional<Note> result = noteRepo.findByIdAndUser(saved.getId(), user);

        assertThat(result).isEmpty();
    }

    @Test
    void findByUserAndTitleContainingIgnoreCase_returnsMatchingNotes() {
        createNote("Spring Boot Guide", "content", List.of(), user);
        createNote("Java Tips", "content", List.of(), user);
        createNote("spring basics", "content", List.of(), user);

        Page<Note> result = noteRepo.findByUserAndTitleContainingIgnoreCase(
                user, "spring", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void findByUserAndTag_returnsNotesWithTag() {
        createNote("Note 1", "content", List.of("work", "ideas"), user);
        createNote("Note 2", "content", List.of("personal"), user);
        createNote("Note 3", "content", List.of("work"), user);

        List<Note> result = noteRepo.findByUserAndTag(user, "work");

        assertThat(result).hasSize(2);
    }

    @Test
    void findByUserAndTag_doesNotReturnOtherUsersNotes() {
        createNote("My Note", "content", List.of("work"), user);
        createNote("Other Note", "content", List.of("work"), otherUser);

        List<Note> result = noteRepo.findByUserAndTag(user, "work");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("My Note");
    }

    @Test
    void pagination_returnsCorrectPage() {
        for (int i = 1; i <= 12; i++) {
            createNote("Note " + i, "content", List.of(), user);
        }

        Page<Note> page0 = noteRepo.findByUser(user, PageRequest.of(0, 5));
        Page<Note> page1 = noteRepo.findByUser(user, PageRequest.of(1, 5));
        Page<Note> page2 = noteRepo.findByUser(user, PageRequest.of(2, 5));

        assertThat(page0.getContent()).hasSize(5);
        assertThat(page1.getContent()).hasSize(5);
        assertThat(page2.getContent()).hasSize(2);
        assertThat(page0.getTotalElements()).isEqualTo(12);
    }
}