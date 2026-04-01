package com.f3cinema.app.repository;

import com.f3cinema.app.config.HibernateUtil;
import com.f3cinema.app.dto.dashboard.DashboardFinance;
import com.f3cinema.app.dto.dashboard.InventoryAlertRow;
import com.f3cinema.app.dto.dashboard.NowShowingRow;
import com.f3cinema.app.dto.dashboard.RevenueSeriesPoint;
import com.f3cinema.app.dto.dashboard.TopMovieRow;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class DashboardRepositoryImpl implements DashboardRepository {

    private static final DashboardRepositoryImpl INSTANCE = new DashboardRepositoryImpl();

    private DashboardRepositoryImpl() {
    }

    public static DashboardRepository getInstance() {
        return INSTANCE;
    }

    @Override
    public DashboardFinance loadFinance() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();
        LocalDateTime yesterdayStart = yesterday.atStartOfDay();
        LocalDateTime yesterdayEnd = today.atStartOfDay();
        LocalDate seriesStart = today.minusDays(6);
        LocalDateTime rangeStart = seriesStart.atStartOfDay();
        LocalDateTime rangeEnd = today.plusDays(1).atStartOfDay();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Today metrics
            BigDecimal revenueToday = queryDecimal(session,
                    "SELECT COALESCE(SUM(final_total), 0) FROM invoices "
                            + "WHERE status = 'PAID' AND created_at >= :ds AND created_at < :de",
                    "ds", todayStart, "de", todayEnd);

            long ticketsSoldToday = queryLong(session,
                    "SELECT COUNT(t.id) FROM tickets t "
                            + "INNER JOIN invoices i ON i.id = t.invoice_id "
                            + "WHERE i.status = 'PAID' AND i.created_at >= :ds AND i.created_at < :de",
                    "ds", todayStart, "de", todayEnd);

            long capacityToday = queryLong(session,
                    "SELECT COALESCE(SUM(cnt), 0) FROM ("
                            + "SELECT (SELECT COUNT(*) FROM seats s WHERE s.room_id = sh.room_id) AS cnt "
                            + "FROM showtimes sh WHERE DATE(sh.start_time) = CURDATE()"
                            + ") cap");

            long ticketsForTodayShowtimes = queryLong(session,
                    "SELECT COUNT(t.id) FROM tickets t "
                            + "INNER JOIN invoices i ON i.id = t.invoice_id "
                            + "INNER JOIN showtimes sh ON sh.id = t.showtime_id "
                            + "WHERE i.status = 'PAID' AND DATE(sh.start_time) = CURDATE()");

            double occupancy = 0.0;
            if (capacityToday > 0) {
                occupancy = (ticketsForTodayShowtimes * 100.0) / capacityToday;
                occupancy = BigDecimal.valueOf(occupancy).setScale(1, RoundingMode.HALF_UP).doubleValue();
            }

            long newCustomersToday = queryLong(session,
                    "SELECT COUNT(*) FROM ("
                            + "SELECT c.id FROM customers c "
                            + "INNER JOIN invoices i ON i.customer_id = c.id "
                            + "WHERE i.status = 'PAID' "
                            + "GROUP BY c.id "
                            + "HAVING MIN(i.created_at) >= :ds AND MIN(i.created_at) < :de"
                            + ") x",
                    "ds", todayStart, "de", todayEnd);

            // Yesterday metrics for comparison
            BigDecimal revenueYesterday = queryDecimal(session,
                    "SELECT COALESCE(SUM(final_total), 0) FROM invoices "
                            + "WHERE status = 'PAID' AND created_at >= :ds AND created_at < :de",
                    "ds", yesterdayStart, "de", yesterdayEnd);

            long ticketsYesterday = queryLong(session,
                    "SELECT COUNT(t.id) FROM tickets t "
                            + "INNER JOIN invoices i ON i.id = t.invoice_id "
                            + "WHERE i.status = 'PAID' AND i.created_at >= :ds AND i.created_at < :de",
                    "ds", yesterdayStart, "de", yesterdayEnd);

            long capacityYesterday = queryLong(session,
                    "SELECT COALESCE(SUM(cnt), 0) FROM ("
                            + "SELECT (SELECT COUNT(*) FROM seats s WHERE s.room_id = sh.room_id) AS cnt "
                            + "FROM showtimes sh WHERE DATE(sh.start_time) = DATE(DATE_SUB(CURDATE(), INTERVAL 1 DAY))"
                            + ") cap");

            long ticketsForYesterdayShowtimes = queryLong(session,
                    "SELECT COUNT(t.id) FROM tickets t "
                            + "INNER JOIN invoices i ON i.id = t.invoice_id "
                            + "INNER JOIN showtimes sh ON sh.id = t.showtime_id "
                            + "WHERE i.status = 'PAID' AND DATE(sh.start_time) = DATE(DATE_SUB(CURDATE(), INTERVAL 1 DAY))");

            double occupancyYesterday = 0.0;
            if (capacityYesterday > 0) {
                occupancyYesterday = (ticketsForYesterdayShowtimes * 100.0) / capacityYesterday;
                occupancyYesterday = BigDecimal.valueOf(occupancyYesterday).setScale(1, RoundingMode.HALF_UP).doubleValue();
            }

            long newCustomersYesterday = queryLong(session,
                    "SELECT COUNT(*) FROM ("
                            + "SELECT c.id FROM customers c "
                            + "INNER JOIN invoices i ON i.customer_id = c.id "
                            + "WHERE i.status = 'PAID' "
                            + "GROUP BY c.id "
                            + "HAVING MIN(i.created_at) >= :ds AND MIN(i.created_at) < :de"
                            + ") x",
                    "ds", yesterdayStart, "de", yesterdayEnd);

            List<Object[]> dayRows = session.createNativeQuery(
                            "SELECT DATE(i.created_at) AS d, COALESCE(SUM(i.final_total), 0) AS amt "
                                    + "FROM invoices i "
                                    + "WHERE i.status = 'PAID' AND i.created_at >= :rs AND i.created_at < :re "
                                    + "GROUP BY DATE(i.created_at) ORDER BY d",
                            Object[].class)
                    .setParameter("rs", rangeStart)
                    .setParameter("re", rangeEnd)
                    .getResultList();

            Map<LocalDate, BigDecimal> byDay = new HashMap<>();
            for (Object[] row : dayRows) {
                LocalDate d = toLocalDate(row[0]);
                byDay.put(d, toBigDecimal(row[1]));
            }
            List<RevenueSeriesPoint> series = new ArrayList<>(7);
            for (int i = 0; i < 7; i++) {
                LocalDate d = seriesStart.plusDays(i);
                series.add(new RevenueSeriesPoint(d, byDay.getOrDefault(d, BigDecimal.ZERO)));
            }

            BigDecimal ticketRev7d = queryDecimal(session,
                    "SELECT COALESCE(SUM(t.final_price), 0) FROM tickets t "
                            + "INNER JOIN invoices i ON i.id = t.invoice_id "
                            + "WHERE i.status = 'PAID' AND i.created_at >= :rs AND i.created_at < :re",
                    "rs", rangeStart, "re", rangeEnd);

            BigDecimal fnbRev7d = queryDecimal(session,
                    "SELECT COALESCE(SUM(ii.quantity * ii.unit_price), 0) FROM invoice_items ii "
                            + "INNER JOIN invoices i ON i.id = ii.invoice_id "
                            + "WHERE i.status = 'PAID' AND i.created_at >= :rs AND i.created_at < :re",
                    "rs", rangeStart, "re", rangeEnd);

            return new DashboardFinance(
                    revenueToday,
                    ticketsSoldToday,
                    occupancy,
                    newCustomersToday,
                    revenueYesterday,
                    ticketsYesterday,
                    occupancyYesterday,
                    newCustomersYesterday,
                    series,
                    ticketRev7d,
                    fnbRev7d
            );
        } catch (Exception e) {
            log.error("Dashboard finance aggregation failed", e);
            throw e;
        }
    }

    @Override
    public List<InventoryAlertRow> loadInventoryAlerts() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = session.createNativeQuery(
                            "SELECT p.name, inv.current_quantity, inv.min_threshold "
                                    + "FROM inventories inv "
                                    + "INNER JOIN products p ON p.id = inv.product_id "
                                    + "WHERE inv.current_quantity < inv.min_threshold",
                            Object[].class)
                    .getResultList();
            List<InventoryAlertRow> list = new ArrayList<>();
            for (Object[] row : rows) {
                String name = row[0] != null ? row[0].toString() : "";
                int cur = ((Number) row[1]).intValue();
                int min = row[2] != null ? ((Number) row[2]).intValue() : 0;
                list.add(new InventoryAlertRow(name, cur, min));
            }
            return list;
        } catch (Exception e) {
            log.error("Inventory alerts query failed", e);
            throw e;
        }
    }

    @Override
    public List<NowShowingRow> loadNowShowingSchedule() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM HH:mm");
        LocalDateTime now = LocalDateTime.now();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = session.createNativeQuery(
                            "SELECT m.title, sh.start_time, sh.end_time "
                                    + "FROM showtimes sh "
                                    + "INNER JOIN movies m ON m.id = sh.movie_id "
                                    + "WHERE m.status = 'NOW_SHOWING' "
                                    + "ORDER BY sh.start_time ASC "
                                    + "LIMIT 40",
                            Object[].class)
                    .getResultList();
            List<NowShowingRow> list = new ArrayList<>();
            for (Object[] row : rows) {
                String title = row[0] != null ? row[0].toString() : "";
                LocalDateTime start = toLocalDateTime(row[1]);
                LocalDateTime end = toLocalDateTime(row[2]);
                String status;
                if (now.isBefore(start)) {
                    status = "Sắp chiếu";
                } else if (!now.isAfter(end)) {
                    status = "Đang chiếu";
                } else {
                    status = "Đã kết thúc";
                }
                String label = status + " · " + start.format(fmt) + " – " + end.format(fmt);
                list.add(new NowShowingRow(title, start, end, label));
            }
            return list;
        } catch (Exception e) {
            log.error("Now showing query failed", e);
            throw e;
        }
    }
    
    @Override
    public List<TopMovieRow> loadTopMovies(int lastNDays, int limit) {
        LocalDate today = LocalDate.now();
        LocalDateTime rangeStart = today.minusDays(lastNDays - 1).atStartOfDay();
        LocalDateTime rangeEnd = today.plusDays(1).atStartOfDay();
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = session.createNativeQuery(
                            "SELECT m.title, COUNT(t.id) AS ticket_count, COALESCE(SUM(t.final_price), 0) AS revenue "
                                    + "FROM tickets t "
                                    + "INNER JOIN invoices i ON i.id = t.invoice_id "
                                    + "INNER JOIN showtimes sh ON sh.id = t.showtime_id "
                                    + "INNER JOIN movies m ON m.id = sh.movie_id "
                                    + "WHERE i.status = 'PAID' AND i.created_at >= :rs AND i.created_at < :re "
                                    + "GROUP BY m.id, m.title "
                                    + "ORDER BY ticket_count DESC "
                                    + "LIMIT :lim",
                            Object[].class)
                    .setParameter("rs", rangeStart)
                    .setParameter("re", rangeEnd)
                    .setParameter("lim", limit)
                    .getResultList();
            
            List<TopMovieRow> result = new ArrayList<>();
            int rank = 1;
            for (Object[] row : rows) {
                String title = row[0] != null ? row[0].toString() : "";
                long ticketCount = ((Number) row[1]).longValue();
                BigDecimal revenue = toBigDecimal(row[2]);
                result.add(new TopMovieRow(rank++, title, ticketCount, revenue));
            }
            return result;
        } catch (Exception e) {
            log.error("Top movies query failed", e);
            throw e;
        }
    }

    private static BigDecimal queryDecimal(Session session, String sql,
            String p1n, LocalDateTime p1, String p2n, LocalDateTime p2) {
        Object o = session.createNativeQuery(sql, Object.class)
                .setParameter(p1n, p1)
                .setParameter(p2n, p2)
                .getSingleResult();
        return toBigDecimal(o);
    }

    private static long queryLong(Session session, String sql,
            String p1n, LocalDateTime p1, String p2n, LocalDateTime p2) {
        Object o = session.createNativeQuery(sql, Object.class)
                .setParameter(p1n, p1)
                .setParameter(p2n, p2)
                .getSingleResult();
        return o instanceof Number n ? n.longValue() : Long.parseLong(o.toString());
    }

    private static long queryLong(Session session, String sql) {
        Object o = session.createNativeQuery(sql, Object.class).getSingleResult();
        return o instanceof Number n ? n.longValue() : Long.parseLong(o.toString());
    }

    private static BigDecimal toBigDecimal(Object o) {
        if (o == null) {
            return BigDecimal.ZERO;
        }
        if (o instanceof BigDecimal b) {
            return b;
        }
        if (o instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        return new BigDecimal(o.toString());
    }

    private static LocalDate toLocalDate(Object o) {
        if (o instanceof java.sql.Date d) {
            return d.toLocalDate();
        }
        if (o instanceof Timestamp ts) {
            return ts.toLocalDateTime().toLocalDate();
        }
        if (o instanceof LocalDate ld) {
            return ld;
        }
        return LocalDate.parse(o.toString());
    }

    private static LocalDateTime toLocalDateTime(Object o) {
        if (o instanceof Timestamp ts) {
            return ts.toLocalDateTime();
        }
        if (o instanceof LocalDateTime ldt) {
            return ldt;
        }
        throw new IllegalArgumentException("Unsupported time type: " + (o != null ? o.getClass() : null));
    }
}
