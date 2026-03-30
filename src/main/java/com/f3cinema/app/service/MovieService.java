package com.f3cinema.app.service;

import com.f3cinema.app.dto.MovieSummaryDTO;
import com.f3cinema.app.entity.Movie;
import com.f3cinema.app.entity.enums.MovieStatus;
import com.f3cinema.app.repository.MovieRepository;
import com.f3cinema.app.repository.MovieRepositoryImpl;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Optional;

/**
 * MovieService — Business logic layer for Movie management.
 * Follows Repository Pattern & Singleton per Backend Standards 2026.
 */
@Log4j2
public class MovieService {

    // Singleton Instance
    private static MovieService instance;

    private final MovieRepository movieRepository;

    private MovieService() {
        this.movieRepository = new MovieRepositoryImpl();
    }

    public static synchronized MovieService getInstance() {
        if (instance == null) {
            instance = new MovieService();
        }
        return instance;
    }

    /**
     * Load all movies.
     */
    public List<Movie> getAllMovies() {
        log.info("Fetching all movies");
        return movieRepository.findAll();
    }

    /** Lấy một Movie theo ID — dùng nội bộ trong Service (không export cho UI). */
    public Movie getMovieById(Long id) {
        if (id == null) return null;
        return movieRepository.findAll().stream()
                .filter(m -> m.getId().equals(id))
                .findFirst().orElse(null);
    }

    /**
     * Search movies by title keyword (debounced from UI).
     */
    public List<Movie> searchByTitle(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllMovies();
        }
        log.info("Searching movies with keyword: {}", keyword);
        return movieRepository.findByTitle(keyword);
    }

    /**
     * Add a new movie — validates business rules.
     */
    public Movie addMovie(Movie movie) {
        validateMovie(movie);
        Movie saved = movieRepository.save(movie);
        log.info("Movie added: {}", saved.getTitle());
        return saved;
    }

    /**
     * Update an existing movie.
     */
    public Movie updateMovie(Movie movie) {
        if (movie.getId() == null) {
            throw new IllegalArgumentException("Movie ID cannot be null for update.");
        }
        validateMovie(movie);
        Movie updated = movieRepository.update(movie);
        log.info("Movie updated: {}", updated.getTitle());
        return updated;
    }

    /**
     * Delete a movie by ID.
     */
    public void deleteMovie(Long id) {
        Optional<Movie> movie = movieRepository.findById(id);
        movie.ifPresentOrElse(
                m -> {
                    movieRepository.delete(m);
                    log.info("Movie deleted: {}", m.getTitle());
                },
                () -> {
                    throw new IllegalArgumentException("Movie not found with ID: " + id);
                }
        );
    }

    /**
     * Filter movies by status.
     */
    public List<Movie> getMoviesByStatus(MovieStatus status) {
        log.info("Filtering movies by status: {}", status);
        return movieRepository.findByStatus(status);
    }

    /**
     * Find a movie by ID.
     */
    public Optional<Movie> findById(Long id) {
        return movieRepository.findById(id);
    }

    /**
     * Trả về danh sách phim rút gọn (ID và Title) chuyên dùng cho ComboBox UI.
     */
    public List<MovieSummaryDTO> getMovieSummaries() {
        return getAllMovies().stream()
                .map(m -> new MovieSummaryDTO(m.getId(), m.getTitle()))
                .collect(java.util.stream.Collectors.toList());
    }

    // ---- Private Validation ----
    private void validateMovie(Movie movie) {
        if (movie.getTitle() == null || movie.getTitle().isBlank()) {
            throw new IllegalArgumentException("Tên phim không được để trống.");
        }
        if (movie.getDuration() == null || movie.getDuration() <= 0) {
            throw new IllegalArgumentException("Thời lượng phim phải lớn hơn 0.");
        }
        if (movie.getStatus() == null) {
            throw new IllegalArgumentException("Trạng thái phim không được để trống.");
        }
    }
}
