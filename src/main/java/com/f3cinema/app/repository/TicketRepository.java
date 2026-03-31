package com.f3cinema.app.repository;

import com.f3cinema.app.entity.Ticket;
import java.util.List;

public interface TicketRepository extends BaseRepository<Ticket, Long> {
    List<Ticket> findByShowtimeId(Long showtimeId);
}
