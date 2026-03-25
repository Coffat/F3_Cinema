package com.f3cinema.app.repository;

import com.f3cinema.app.entity.Seat;
import java.util.List;

public interface SeatRepository extends BaseRepository<Seat, Long> {
    List<Seat> findByRoomId(Long roomId);
}
