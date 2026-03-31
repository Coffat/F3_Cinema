package com.f3cinema.app.repository;

import com.f3cinema.app.config.HibernateUtil;
import com.f3cinema.app.entity.Ticket;
import org.hibernate.Session;
import java.util.List;

public class TicketRepositoryImpl extends BaseRepositoryImpl<Ticket, Long> implements TicketRepository {
    public TicketRepositoryImpl() {
        super(Ticket.class);
    }

    @Override
    public List<Ticket> findByShowtimeId(Long showtimeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT t FROM Ticket t JOIN FETCH t.seat WHERE t.showtime.id = :showtimeId", Ticket.class)
                    .setParameter("showtimeId", showtimeId)
                    .list();
        }
    }
}
