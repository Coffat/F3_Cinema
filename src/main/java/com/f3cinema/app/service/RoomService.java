package com.f3cinema.app.service;

import com.f3cinema.app.config.HibernateUtil;
import com.f3cinema.app.entity.Room;
import com.f3cinema.app.entity.Seat;
import com.f3cinema.app.entity.enums.SeatType;
import com.f3cinema.app.repository.RoomRepository;
import com.f3cinema.app.repository.RoomRepositoryImpl;
import com.f3cinema.app.repository.SeatRepository;
import com.f3cinema.app.repository.SeatRepositoryImpl;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

@Log4j2
public class RoomService {
    private final RoomRepository roomRepository = new RoomRepositoryImpl();
    private final SeatRepository seatRepository = new SeatRepositoryImpl();

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }
    
    public List<Seat> getSeatsByRoom(Long roomId) {
        return seatRepository.findByRoomId(roomId);
    }

    public void updateRoom(Room room) {
        roomRepository.update(room);
    }

    public void deleteRoom(Room room) {
        roomRepository.delete(room);
    }

    /**
     * Transactional creation of Room and its Seats.
     * Generates a matrix of seats (A-J, 1-N etc.) default type NORMAL.
     */
    public void saveRoomWithSeats(Room room, int rows, int cols) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // Persist the room first
            session.persist(room);
            
            // Generate seats
            for (int r = 0; r < rows; r++) {
                char rowChar = (char) ('A' + r);
                for (int c = 1; c <= cols; c++) {
                    Seat seat = Seat.builder()
                            .rowChar(String.valueOf(rowChar))
                            .number(c)
                            .seatType(SeatType.NORMAL)
                            .room(room)
                            .build();
                    session.persist(seat);
                }
            }
            
            transaction.commit();
            log.info("Successfully created room and generated seats: {} rows, {} cols.", rows, cols);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            log.error("Failed to sequence room creation. Transaciton rolled back.", e);
            throw new RuntimeException("Error executing room with seats save operation", e);
        }
    }
    
    public void updateSeat(Seat seat) {
        seatRepository.update(seat);
    }
}
