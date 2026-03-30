package com.f3cinema.app.repository;

import com.f3cinema.app.config.HibernateUtil;
import com.f3cinema.app.entity.Showtime;
import org.hibernate.Session;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * ShowtimeRepositoryImpl - Triển khai các truy vấn liên quan đến suất chiếu.
 * Sử dụng JOIN FETCH để tải trước dữ liệu phim và phòng, tránh N+1 Query.
 */
public class ShowtimeRepositoryImpl extends BaseRepositoryImpl<Showtime, Long> implements ShowtimeRepository {
    
    public ShowtimeRepositoryImpl() {
        super(Showtime.class);
    }

    @Override
    public List<Showtime> findByFilter(LocalDate date, Long movieId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

            StringBuilder hql = new StringBuilder("SELECT s FROM Showtime s ");
            // Tối ưu hiệu năng bằng JOIN FETCH
            hql.append("JOIN FETCH s.movie ");
            hql.append("JOIN FETCH s.room ");
            hql.append("WHERE s.startTime >= :start AND s.startTime <= :end ");

            if (movieId != null) {
                hql.append("AND s.movie.id = :movieId ");
            }
            
            hql.append("ORDER BY s.startTime ASC");

            var query = session.createQuery(hql.toString(), Showtime.class)
                    .setParameter("start", startOfDay)
                    .setParameter("end", endOfDay);

            if (movieId != null) {
                query.setParameter("movieId", movieId);
            }

            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi truy vấn danh sách suất chiếu từ Database", e);
        }
    }

    @Override
    public boolean existsConflict(Long roomId, java.time.LocalDateTime start, java.time.LocalDateTime end, Long excludeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("SELECT COUNT(s) FROM Showtime s ");
            hql.append("WHERE s.room.id = :roomId ");
            hql.append("AND s.startTime < :end ");
            hql.append("AND s.endTime > :start ");
            
            if (excludeId != null) {
                hql.append("AND s.id != :excludeId ");
            }

            var query = session.createQuery(hql.toString(), Long.class)
                    .setParameter("roomId", roomId)
                    .setParameter("start", start)
                    .setParameter("end", end);

            if (excludeId != null) {
                query.setParameter("excludeId", excludeId);
            }

            return query.uniqueResult() > 0;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi kiểm tra trùng lịch chiếu", e);
        }
    }
}
