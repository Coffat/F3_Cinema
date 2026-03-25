package com.f3cinema.app.repository;

import com.f3cinema.app.config.HibernateUtil;
import com.f3cinema.app.entity.Seat;
import org.hibernate.Session;
import java.util.List;

public class SeatRepositoryImpl extends BaseRepositoryImpl<Seat, Long> implements SeatRepository {
    public SeatRepositoryImpl() {
        super(Seat.class);
    }

    @Override
    public List<Seat> findByRoomId(Long roomId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Seat s join fetch s.room r where r.id = :roomId order by s.rowChar, s.number", Seat.class)
                    .setParameter("roomId", roomId)
                    .list();
        }
    }
}
