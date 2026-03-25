package com.f3cinema.app.service;

import com.f3cinema.app.entity.Genre;
import com.f3cinema.app.repository.GenreRepository;
import com.f3cinema.app.repository.GenreRepositoryImpl;

import java.util.List;

public class GenreService {
    private static GenreService instance;
    private final GenreRepository genreRepository;

    private GenreService() {
        this.genreRepository = new GenreRepositoryImpl();
    }

    public static synchronized GenreService getInstance() {
        if (instance == null) {
            instance = new GenreService();
        }
        return instance;
    }

    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }
}
