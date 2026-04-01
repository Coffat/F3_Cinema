package com.f3cinema.app.repository;

import com.f3cinema.app.config.HibernateUtil;
import com.f3cinema.app.entity.User;
import org.hibernate.Session;
import com.f3cinema.app.entity.enums.UserRole;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity, extending the Generic BaseRepositoryImpl.
 */
public class UserRepository extends BaseRepositoryImpl<User, Long> {

    public UserRepository() {
        super(User.class);
    }

    /**
     * Find a User by their username (case-insensitive).
     */
    public Optional<User> findByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM User u WHERE lower(u.username) = lower(:username)", User.class)
                    .setParameter("username", username)
                    .uniqueResultOptional();
        }
    }

    /**
     * List all staff accounts.
     */
    public List<User> findAllStaff() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM User u WHERE u.role = :role ORDER BY u.id DESC",
                            User.class)
                    .setParameter("role", UserRole.STAFF)
                    .list();
        }
    }

    /**
     * Search staff accounts by username/fullName (case-insensitive).
     */
    public List<User> searchStaff(String keyword) {
        String k = (keyword == null) ? "" : keyword.trim().toLowerCase();
        if (k.isBlank()) return findAllStaff();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM User u " +
                                    "WHERE u.role = :role " +
                                    "AND (lower(u.username) LIKE :kw OR lower(u.fullName) LIKE :kw) " +
                                    "ORDER BY u.id DESC",
                            User.class)
                    .setParameter("role", UserRole.STAFF)
                    .setParameter("kw", "%" + k + "%")
                    .list();
        }
    }
}
