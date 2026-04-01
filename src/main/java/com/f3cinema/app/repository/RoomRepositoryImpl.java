package com.f3cinema.app.repository;

import com.f3cinema.app.config.HibernateUtil;
import com.f3cinema.app.entity.Room;
import org.hibernate.Session;
import java.util.List;
import java.util.Optional;

public class RoomRepositoryImpl extends BaseRepositoryImpl<Room, Long> implements RoomRepository {
    public RoomRepositoryImpl() {
        super(Room.class);
    }

    @Override
    public Optional<Room> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Room room = session.createQuery(
                "SELECT r FROM Room r LEFT JOIN FETCH r.seats WHERE r.id = :id", Room.class)
                .setParameter("id", id)
                .uniqueResult();
            return Optional.ofNullable(room);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public List<Room> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "SELECT DISTINCT r FROM Room r LEFT JOIN FETCH r.seats", Room.class)
                .list();
        } catch (Exception e) {
            throw e;
        }
    }
}
