package com.rugved.notes.controller;

import com.rugved.notes.dto.NoteRequestDto;
import com.rugved.notes.dto.NoteResponseDto;
import com.rugved.notes.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notes")
@CrossOrigin
public class NoteController {

    private final NoteService service;

    public NoteController(NoteService service) {
        this.service = service;
    }

    @GetMapping
    public Page<NoteResponseDto> getAll(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return service.getAll(userDetails.getUsername(), pageable);
    }

    @GetMapping("/{id}")
    public NoteResponseDto getById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return service.getById(userDetails.getUsername(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NoteResponseDto create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody NoteRequestDto request) {
        return service.create(userDetails.getUsername(), request);
    }

    @PutMapping("/{id}")
    public NoteResponseDto update(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody NoteRequestDto request) {
        return service.update(userDetails.getUsername(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        service.delete(userDetails.getUsername(), id);
    }

    @GetMapping("/search")
    public Page<NoteResponseDto> search(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String title,
            @PageableDefault(size = 10) Pageable pageable) {
        return service.searchByTitle(userDetails.getUsername(), title, pageable);
    }

    @GetMapping("/tag/{tag}")
    public List<NoteResponseDto> getByTag(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String tag) {
        return service.getByTag(userDetails.getUsername(), tag);
    }
}