package com.f3cinema.app.repository;

import com.f3cinema.app.config.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import lombok.extern.log4j.Log4j2;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of Generic Base Repository with Transaction Management.
 */
@Log4j2
public class BaseRepositoryImpl<T, ID> implements BaseRepository<T, ID> {
    private final Class<T> clazz;

    public BaseRepositoryImpl(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T save(T entity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(entity);
            transaction.commit();
            log.debug("Saved entity: {}", entity);
            return entity;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            log.error("Error saving entity: {}", entity, e);
            throw e;
        }
    }

    @Override
    public T update(T entity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            T merged = session.merge(entity);
            transaction.commit();
            log.debug("Updated entity: {}", merged);
            return merged;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            log.error("Error updating entity: {}", entity, e);
            throw e;
        }
    }

    @Override
    public void delete(T entity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(session.contains(entity) ? entity : session.merge(entity));
            transaction.commit();
            log.debug("Deleted entity: {}", entity);
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            log.error("Error deleting entity: {}", entity, e);
            throw e;
        }
    }

    @Override
    public Optional<T> findById(ID id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            T entity = session.get(clazz, id);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            log.error("Error finding entity by id: {}", id, e);
            throw e;
        }
    }

    @Override
    public List<T> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from " + clazz.getName(), clazz).list();
        } catch (Exception e) {
            log.error("Error finding all entities of type: {}", clazz.getName(), e);
            throw e;
        }
    }
}
