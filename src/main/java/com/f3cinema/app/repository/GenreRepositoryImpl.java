package com.f3cinema.app.repository;

import com.f3cinema.app.entity.Genre;

public class GenreRepositoryImpl extends BaseRepositoryImpl<Genre, Long> implements GenreRepository {
    public GenreRepositoryImpl() {
        super(Genre.class);
    }
}
