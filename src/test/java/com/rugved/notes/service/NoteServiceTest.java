package com.rugved.notes.service;

import com.rugved.notes.dto.NoteRequestDto;
import com.rugved.notes.dto.NoteResponseDto;
import com.rugved.notes.exception.NoteNotFoundException;
import com.rugved.notes.model.Note;
import com.rugved.notes.model.User;
import com.rugved.notes.repository.NoteRepository;
import com.rugved.notes.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock NoteRepository noteRepo;
    @Mock UserRepository userRepo;

    @InjectMocks NoteServiceImpl service;

    private User user;
    private Note note;
    private NoteRequestDto request;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@test.com");

        note = new Note();
        note.setTitle("Test Note");
        note.setContent("Test content");
        note.setTags(List.of("work", "ideas"));
        note.setUser(user);

        request = new NoteRequestDto();
        request.setTitle("Test Note");
        request.setContent("Test content");
        request.setTags(List.of("work", "ideas"));
    }

    @Test
    void create_savesAndReturnsNote() {
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(noteRepo.save(any(Note.class))).thenReturn(note);

        NoteResponseDto result = service.create("test@test.com", request);

        assertThat(result.getTitle()).isEqualTo("Test Note");
        assertThat(result.getContent()).isEqualTo("Test content");
        assertThat(result.getTags()).containsExactly("work", "ideas");
        verify(noteRepo, times(1)).save(any(Note.class));
    }

    @Test
    void getById_returnsNote_whenFound() {
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(noteRepo.findByIdAndUser(1L, user)).thenReturn(Optional.of(note));

        NoteResponseDto result = service.getById("test@test.com", 1L);

        assertThat(result.getTitle()).isEqualTo("Test Note");
    }

    @Test
    void getById_throwsException_whenNotFound() {
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(noteRepo.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById("test@test.com", 99L))
                .isInstanceOf(NoteNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void update_updatesAndReturnsNote_whenFound() {
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(noteRepo.findByIdAndUser(1L, user)).thenReturn(Optional.of(note));
        when(noteRepo.save(any(Note.class))).thenReturn(note);

        request.setTitle("Updated title");
        NoteResponseDto result = service.update("test@test.com", 1L, request);

        assertThat(result.getTitle()).isEqualTo("Updated title");
        verify(noteRepo, times(1)).save(any(Note.class));
    }

    @Test
    void update_throwsException_whenNotFound() {
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(noteRepo.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update("test@test.com", 99L, request))
                .isInstanceOf(NoteNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void delete_deletesSuccessfully_whenFound() {
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(noteRepo.findByIdAndUser(1L, user)).thenReturn(Optional.of(note));

        service.delete("test@test.com", 1L);

        verify(noteRepo, times(1)).delete(note);
    }

    @Test
    void delete_throwsException_whenNotFound() {
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(noteRepo.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete("test@test.com", 99L))
                .isInstanceOf(NoteNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_throwsException_whenUserNotFound() {
        when(userRepo.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create("unknown@test.com", request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }
}