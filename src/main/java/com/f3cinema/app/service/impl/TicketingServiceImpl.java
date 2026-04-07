package com.f3cinema.app.service.impl;

import com.f3cinema.app.config.HibernateUtil;
import com.f3cinema.app.dto.SeatDTO;
import com.f3cinema.app.dto.ShowtimeSummaryDTO;
import com.f3cinema.app.entity.*;
import com.f3cinema.app.entity.enums.InvoiceStatus;
import com.f3cinema.app.entity.enums.PaymentMethod;
import com.f3cinema.app.entity.enums.PaymentStatus;
import com.f3cinema.app.entity.enums.PointRedemptionTier;
import com.f3cinema.app.repository.*;
import com.f3cinema.app.service.CustomerService;
import com.f3cinema.app.service.TicketingService;
import com.f3cinema.app.util.SessionManager;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TicketingServiceImpl - Implementation using Hibernate 6 and Real Database.
 * Compliant with Backend Development Standards §2.1 & §4.
 */
@Log4j2
public class TicketingServiceImpl implements TicketingService {

    private static final TicketingServiceImpl INSTANCE = new TicketingServiceImpl();

    private final ShowtimeRepository showtimeRepository = new ShowtimeRepositoryImpl();
    private final SeatRepository seatRepository = new SeatRepositoryImpl();
    private final TicketRepository ticketRepository = new TicketRepositoryImpl();
    private final InvoiceRepository invoiceRepository = new InvoiceRepositoryImpl();
    private final CustomerService customerService = CustomerServiceImpl.getInstance();

    private TicketingServiceImpl() {
    }

    public static TicketingServiceImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public List<SeatDTO> getSeatsForShowtime(Long showtimeId) {
        log.info("Fetching real-time seat map for showtime ID: {}", showtimeId);

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // 1. Get Showtime to identify the room
            Showtime showtime = session.createQuery(
                    "SELECT s FROM Showtime s JOIN FETCH s.room WHERE s.id = :id", Showtime.class)
                    .setParameter("id", showtimeId)
                    .uniqueResult();

            if (showtime == null)
                throw new RuntimeException("Không tìm thấy suất chiếu ID: " + showtimeId);

            // 2. Get all seats for that room
            List<Seat> seats = seatRepository.findByRoomId(showtime.getRoom().getId());

            // 3. Get all sold tickets for this showtime to determine occupation
            Set<Long> soldSeatIds = ticketRepository.findByShowtimeId(showtimeId)
                    .stream()
                    .map(t -> t.getSeat().getId())
                    .collect(Collectors.toSet());

            // 4. Map to DTOs
            return seats.stream().map(s -> {
                boolean isSold = soldSeatIds.contains(s.getId());
                double price = calculatePrice(showtime.getBasePrice(), s.getSeatType());

                return new SeatDTO(
                        s.getId(),
                        s.getRowChar(),
                        s.getNumber(),
                        mapToDtoSeatType(s.getSeatType()),
                        price,
                        isSold);
            }).collect(Collectors.toList());
        }
    }

    @Override
    public ShowtimeSummaryDTO getShowtimeSummary(Long showtimeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Showtime s = session.createQuery(
                    "SELECT s FROM Showtime s JOIN FETCH s.movie JOIN FETCH s.room WHERE s.id = :id", Showtime.class)
                    .setParameter("id", showtimeId)
                    .uniqueResult();

            if (s == null)
                throw new RuntimeException("Suất chiếu không tồn tại.");

            return new ShowtimeSummaryDTO(
                    s.getId(),
                    s.getMovie().getTitle(),
                    s.getRoom().getName() + " (" + s.getRoom().getRoomType() + ")",
                    s.getStartTime(),
                    s.getEndTime(),
                    s.getBasePrice().doubleValue());
        }
    }

    @Override
    public void bookSeats(Long showtimeId, List<Long> seatIds) {
        log.info("Beginning booking transaction for showtime {}. Seats: {}", showtimeId, seatIds);

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // 1. Pre-fetch Showtime (JOIN FETCH tránh lazy sau khi flush/commit)
            Showtime showtime = session.createQuery(
                            "SELECT DISTINCT s FROM Showtime s JOIN FETCH s.room JOIN FETCH s.movie WHERE s.id = :id",
                            Showtime.class)
                    .setParameter("id", showtimeId)
                    .uniqueResult();
            if (showtime == null)
                throw new RuntimeException("Suất chiếu đã bị xóa hoặc không hợp lệ.");

            // 2. Resolve staff as managed reference (SessionManager holds detached User from login)
            User staff = resolveStaffUser(session);

            // 3. Create Master: Invoice
            Invoice invoice = Invoice.builder()
                    .user(staff)
                    .createdAt(LocalDateTime.now())
                    .status(InvoiceStatus.PAID)
                    .totalAmount(BigDecimal.ZERO) // Calculated later
                    .build();

            session.persist(invoice);

            // 4. Create Details: Tickets & Calculate Total
            BigDecimal total = BigDecimal.ZERO;
            List<Ticket> tickets = new ArrayList<>();

            for (Long seatId : seatIds) {
                Seat seat = session.get(Seat.class, seatId);
                if (seat == null)
                    continue;

                BigDecimal finalPrice = BigDecimal.valueOf(calculatePrice(showtime.getBasePrice(), seat.getSeatType()));

                Ticket ticket = Ticket.builder()
                        .invoice(invoice)
                        .showtime(showtime)
                        .seat(seat)
                        .finalPrice(finalPrice)
                        .build();

                session.persist(ticket);
                tickets.add(ticket);
                total = total.add(finalPrice);
            }

            // 5. Update Invoice with total and Create Payment entry
            invoice.setTotalAmount(total);
            session.merge(invoice);

            Payment payment = Payment.builder()
                    .invoice(invoice)
                    .amount(total)
                    .method(PaymentMethod.CASH)
                    .status(PaymentStatus.COMPLETED)
                    .build();

            session.persist(payment);

            tx.commit();
            log.info("Booking completed successfully. Invoice ID: {}", invoice.getId());

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            log.error("Critical error during seat booking transaction: {}", e.getMessage(), e);
            throw new RuntimeException("Giao dịch thất bại: " + e.getMessage());
        }
    }

    @Override
    public Long bookSeatsWithLoyalty(
            Long showtimeId,
            List<Long> seatIds,
            Map<Long, Integer> snacks,
            Long customerId,
            PointRedemptionTier redemptionTier,
            PaymentMethod paymentMethod,
            String externalTransactionId
    ) {
        log.info("Booking with loyalty: showtime={}, seats={}, customer={}, tier={}, payment={}, txn={}",
                showtimeId, seatIds, customerId, redemptionTier, paymentMethod, externalTransactionId);

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            boolean hasSeatSelection = seatIds != null && !seatIds.isEmpty();
            boolean hasSnackItems = snacks != null && !snacks.isEmpty();
            if (!hasSeatSelection && !hasSnackItems) {
                throw new IllegalArgumentException("Đơn hàng trống: cần ít nhất ghế hoặc sản phẩm.");
            }

            Showtime showtime = null;
            if (hasSeatSelection) {
                if (showtimeId == null) {
                    throw new RuntimeException("Cần chọn suất chiếu khi đặt vé.");
                }
                showtime = session.createQuery(
                                "SELECT DISTINCT s FROM Showtime s JOIN FETCH s.room JOIN FETCH s.movie WHERE s.id = :id",
                                Showtime.class)
                        .setParameter("id", showtimeId)
                        .uniqueResult();
                if (showtime == null) {
                    throw new RuntimeException("Suất chiếu không hợp lệ.");
                }
            }

            User staff = resolveStaffUser(session);

            Customer customer = null;
            if (customerId != null) {
                customer = session.get(Customer.class, customerId);
                if (customer == null) {
                    throw new RuntimeException("Khách hàng không tồn tại.");
                }
            }

            BigDecimal ticketsTotal = BigDecimal.ZERO;
            List<Ticket> tickets = new ArrayList<>();

            if (hasSeatSelection && showtime != null) {
                for (Long seatId : seatIds) {
                    Seat seat = session.get(Seat.class, seatId);
                    if (seat == null) continue;

                    BigDecimal finalPrice = BigDecimal.valueOf(calculatePrice(showtime.getBasePrice(), seat.getSeatType()));
                    Ticket ticket = Ticket.builder()
                            .showtime(showtime)
                            .seat(seat)
                            .finalPrice(finalPrice)
                            .build();

                    tickets.add(ticket);
                    ticketsTotal = ticketsTotal.add(finalPrice);
                }
            }

            BigDecimal snacksTotal = BigDecimal.ZERO;
            List<InvoiceItem> invoiceItems = new ArrayList<>();

            if (snacks != null && !snacks.isEmpty()) {
                for (Map.Entry<Long, Integer> entry : snacks.entrySet()) {
                    Product product = session.createQuery(
                                    "SELECT p FROM Product p LEFT JOIN FETCH p.inventory WHERE p.id = :id",
                                    Product.class)
                            .setParameter("id", entry.getKey())
                            .uniqueResult();
                    if (product == null) continue;

                    int qty = entry.getValue();
                    BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(qty));

                    InvoiceItem item = InvoiceItem.builder()
                            .product(product)
                            .quantity(qty)
                            .unitPrice(product.getPrice())
                            .build();

                    invoiceItems.add(item);
                    snacksTotal = snacksTotal.add(itemTotal);
                }
            }

            BigDecimal subtotal = ticketsTotal.add(snacksTotal);
            BigDecimal discount = BigDecimal.ZERO;
            Integer pointsUsed = 0;

            if (redemptionTier != null && customer != null) {
                int availablePoints = customer.getPoints() != null ? customer.getPoints() : 0;
                if (availablePoints < redemptionTier.getRequiredPoints()) {
                    throw new IllegalArgumentException("Không đủ điểm để đổi voucher này!");
                }

                discount = subtotal.multiply(BigDecimal.valueOf(redemptionTier.getDiscountPercent()))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                pointsUsed = redemptionTier.getRequiredPoints();

                customer.setPoints(availablePoints - pointsUsed);
                session.merge(customer);
                log.info("Redeemed {} points for {}% discount", pointsUsed, redemptionTier.getDiscountPercent());
            }

            BigDecimal finalTotal = subtotal.subtract(discount);
            int pointsEarned = customerService.calculatePointsFromAmount(finalTotal);

            Invoice invoice = Invoice.builder()
                    .user(staff)
                    .customer(customer)
                    .totalAmount(finalTotal)
                    .pointsUsed(pointsUsed)
                    .pointsEarned(pointsEarned)
                    .createdAt(LocalDateTime.now())
                    .status(InvoiceStatus.PAID)
                    .build();

            session.persist(invoice);

            for (Ticket ticket : tickets) {
                ticket.setInvoice(invoice);
                session.persist(ticket);
            }

            for (InvoiceItem item : invoiceItems) {
                item.setInvoice(invoice);
                session.persist(item);
            }

            Payment payment = Payment.builder()
                    .invoice(invoice)
                    .amount(finalTotal)
                    .method(paymentMethod != null ? paymentMethod : PaymentMethod.CASH)
                    .status(PaymentStatus.COMPLETED)
                    .transactionId(externalTransactionId)
                    .build();
            session.persist(payment);

            if (customer != null && pointsEarned > 0) {
                int currentPoints = customer.getPoints() != null ? customer.getPoints() : 0;
                customer.setPoints(currentPoints + pointsEarned);
                session.merge(customer);
                log.info("Customer earned {} points. New balance: {}", pointsEarned, customer.getPoints());
            }

            tx.commit();
            long invoiceId = invoice.getId();
            log.info("Booking with loyalty completed. Invoice ID: {}", invoiceId);
            return invoiceId;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            log.error("Error during loyalty booking: {}", e.getMessage(), e);
            throw new RuntimeException("Giao dịch thất bại: " + e.getMessage());
        }
    }

    /**
     * Dùng proxy/reference trong session hiện tại — tránh gắn User detached từ login (session đã đóng)
     * gây lỗi JDBC/Hibernate kiểu "LogicalConnectionManagedImpl is closed".
     */
    private User resolveStaffUser(Session session) {
        User current = SessionManager.getCurrentUser();
        if (current != null && current.getId() != null) {
            return session.getReference(User.class, current.getId());
        }
        User fallback = session.createQuery("from User u where u.role = 'ADMIN'", User.class)
                .setMaxResults(1)
                .uniqueResult();
        if (fallback == null) {
            throw new RuntimeException("Không tìm thấy tài khoản nhân viên để ghi hóa đơn.");
        }
        return fallback;
    }

    // ── Helper Methods ──────────────────────────────────────────────────

    private double calculatePrice(BigDecimal base, com.f3cinema.app.entity.enums.SeatType type) {
        double result = base.doubleValue();
        // Surcharge logic based on seat type
        switch (type) {
            case VIP -> result += 20000;
            case SWEETBOX -> result += 40000;
            default -> {
            } // Normal
        }
        return result;
    }

    private SeatDTO.SeatType mapToDtoSeatType(com.f3cinema.app.entity.enums.SeatType entityType) {
        return switch (entityType) {
            case VIP -> SeatDTO.SeatType.VIP;
            case SWEETBOX -> SeatDTO.SeatType.SWEETBOX;
            default -> SeatDTO.SeatType.NORMAL;
        };
    }
}
