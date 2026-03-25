package com.f3cinema.app.repository;

import com.f3cinema.app.config.HibernateUtil;
import com.f3cinema.app.entity.Movie;
import com.f3cinema.app.entity.enums.MovieStatus;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;

import java.util.List;

/**
 * MovieRepositoryImpl — Hibernate 6 implementation of MovieRepository.
 * Uses LAZY loading and HQL queries per Backend Standards 2026.
 */
@Log4j2
public class MovieRepositoryImpl extends BaseRepositoryImpl<Movie, Long> implements MovieRepository {

    public MovieRepositoryImpl() {
        super(Movie.class);
    }

    @Override
    public List<Movie> findByTitle(String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT m FROM Movie m LEFT JOIN FETCH m.genres WHERE LOWER(m.title) LIKE LOWER(:keyword)";
            return session.createQuery(hql, Movie.class)
                    .setParameter("keyword", "%" + keyword + "%")
                    .list();
        } catch (Exception e) {
            log.error("Error searching movies by title: {}", keyword, e);
            throw e;
        }
    }

    @Override
    public List<Movie> findByStatus(MovieStatus status) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT m FROM Movie m LEFT JOIN FETCH m.genres WHERE m.status = :status";
            return session.createQuery(hql, Movie.class)
                    .setParameter("status", status)
                    .list();
        } catch (Exception e) {
            log.error("Error filtering movies by status: {}", status, e);
            throw e;
        }
    }

    @Override
    public List<Movie> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT DISTINCT m FROM Movie m LEFT JOIN FETCH m.genres", Movie.class).list();
        } catch (Exception e) {
            log.error("Error finding all movies", e);
            throw e;
        }
    }
}
