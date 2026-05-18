package com.rugved.notes.service;

import com.rugved.notes.dto.NoteRequestDto;
import com.rugved.notes.dto.NoteResponseDto;
import com.rugved.notes.exception.NoteNotFoundException;
import com.rugved.notes.model.Note;
import com.rugved.notes.model.User;
import com.rugved.notes.repository.NoteRepository;
import com.rugved.notes.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepo;
    private final UserRepository userRepo;

    public NoteServiceImpl(NoteRepository noteRepo, UserRepository userRepo) {
        this.noteRepo = noteRepo;
        this.userRepo = userRepo;
    }

    @Override
    public Page<NoteResponseDto> getAll(String email, Pageable pageable) {
        User user = findUser(email);
        return noteRepo.findByUser(user, pageable).map(this::toResponse);
    }

    @Override
    public NoteResponseDto getById(String email, Long id) {
        User user = findUser(email);
        Note note = noteRepo.findByIdAndUser(id, user)
                .orElseThrow(() -> new NoteNotFoundException(id));
        return toResponse(note);
    }

    @Override
    public NoteResponseDto create(String email, NoteRequestDto request) {
        User user = findUser(email);
        Note note = new Note();
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setTags(request.getTags());
        note.setUser(user);
        return toResponse(noteRepo.save(note));
    }

    @Override
    public NoteResponseDto update(String email, Long id, NoteRequestDto request) {
        User user = findUser(email);
        Note note = noteRepo.findByIdAndUser(id, user)
                .orElseThrow(() -> new NoteNotFoundException(id));
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setTags(request.getTags());
        return toResponse(noteRepo.save(note));
    }

    @Override
    public void delete(String email, Long id) {
        User user = findUser(email);
        Note note = noteRepo.findByIdAndUser(id, user)
                .orElseThrow(() -> new NoteNotFoundException(id));
        noteRepo.delete(note);
    }

    @Override
    public Page<NoteResponseDto> searchByTitle(String email, String title, Pageable pageable) {
        User user = findUser(email);
        return noteRepo.findByUserAndTitleContainingIgnoreCase(user, title, pageable)
                .map(this::toResponse);
    }

    @Override
    public List<NoteResponseDto> getByTag(String email, String tag) {
        User user = findUser(email);
        return noteRepo.findByUserAndTag(user, tag)
                .stream().map(this::toResponse).toList();
    }

    private User findUser(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    private NoteResponseDto toResponse(Note note) {
        return new NoteResponseDto(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getTags(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }
}