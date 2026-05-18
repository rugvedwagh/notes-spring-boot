package com.rugved.notes.repository;

import com.rugved.notes.model.Note;
import com.rugved.notes.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    // get all notes for a user with pagination
    Page<Note> findByUser(User user, Pageable pageable);

    // find a specific note by id only if it belongs to the user
    Optional<Note> findByIdAndUser(Long id, User user);

    // search by title (case insensitive)
    Page<Note> findByUserAndTitleContainingIgnoreCase(User user, String title, Pageable pageable);

    // find notes by tag
    @Query("SELECT n FROM Note n JOIN n.tags t WHERE n.user = :user AND t = :tag")
    List<Note> findByUserAndTag(@Param("user") User user, @Param("tag") String tag);
}