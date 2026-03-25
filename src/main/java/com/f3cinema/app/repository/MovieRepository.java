package com.f3cinema.app.repository;

import com.f3cinema.app.entity.Movie;
import com.f3cinema.app.entity.enums.MovieStatus;

import java.util.List;

/**
 * Repository interface for Movie entity.
 * Extends BaseRepository for generic CRUD operations.
 */
public interface MovieRepository extends BaseRepository<Movie, Long> {

    /**
     * Search movies by title (case-insensitive LIKE).
     */
    List<Movie> findByTitle(String keyword);

    /**
     * Filter movies by status.
     */
    List<Movie> findByStatus(MovieStatus status);
}
