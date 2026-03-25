package com.f3cinema.app.repository;

import com.f3cinema.app.config.HibernateUtil;
import com.f3cinema.app.entity.User;
import org.hibernate.Session;
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
}
