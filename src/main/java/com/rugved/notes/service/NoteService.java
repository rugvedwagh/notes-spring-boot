package com.rugved.notes.service;

import com.rugved.notes.dto.NoteRequestDto;
import com.rugved.notes.dto.NoteResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NoteService {
    Page<NoteResponseDto> getAll(String email, Pageable pageable);
    NoteResponseDto getById(String email, Long id);
    NoteResponseDto create(String email, NoteRequestDto request);
    NoteResponseDto update(String email, Long id, NoteRequestDto request);
    void delete(String email, Long id);
    Page<NoteResponseDto> searchByTitle(String email, String title, Pageable pageable);
    List<NoteResponseDto> getByTag(String email, String tag);
}