package com.f3cinema.app.repository;

import com.f3cinema.app.config.HibernateUtil;
import com.f3cinema.app.dto.transaction.PaymentLineDTO;
import com.f3cinema.app.dto.transaction.SnackLineDTO;
import com.f3cinema.app.dto.transaction.TicketLineDTO;
import com.f3cinema.app.dto.transaction.TransactionRowDTO;
import com.f3cinema.app.entity.Invoice;
import com.f3cinema.app.entity.enums.InvoiceStatus;
import com.f3cinema.app.entity.enums.PaymentStatus;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.MutationQuery;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Log4j2
public class InvoiceRepositoryImpl extends BaseRepositoryImpl<Invoice, Long> implements InvoiceRepository {
    public InvoiceRepositoryImpl() {
        super(Invoice.class);
    }

    @Override
    public List<TransactionRowDTO> searchRows(
            String keyword,
            LocalDate fromDate,
            LocalDate toDate,
            InvoiceStatus invoiceStatus,
            PaymentStatus paymentStatus,
            Long staffId,
            int offset,
            int limit
    ) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("""
                    SELECT new com.f3cinema.app.dto.transaction.TransactionRowDTO(
                        i.id, i.createdAt,
                        coalesce(c.fullName, 'Khách lẻ'),
                        c.phone,
                        u.fullName,
                        i.totalAmount,
                        i.status,
                        p.status
                    )
                    FROM Invoice i
                    JOIN i.user u
                    LEFT JOIN i.customer c
                    LEFT JOIN Payment p ON p.id = (
                        SELECT max(p2.id) FROM Payment p2 WHERE p2.invoice.id = i.id
                    )
                    WHERE 1=1
                    """);
            applyFilters(hql, keyword, fromDate, toDate, invoiceStatus, paymentStatus, staffId);
            hql.append(" ORDER BY i.createdAt DESC, i.id DESC");

            Query<TransactionRowDTO> query = session.createQuery(hql.toString(), TransactionRowDTO.class);
            bindFilters(query, keyword, fromDate, toDate, invoiceStatus, paymentStatus, staffId);
            return query.setFirstResult(Math.max(offset, 0))
                    .setMaxResults(limit <= 0 ? 20 : limit)
                    .list();
        } catch (Exception e) {
            log.error("Error searching invoices", e);
            throw e;
        }
    }

    @Override
    public long countRows(
            String keyword,
            LocalDate fromDate,
            LocalDate toDate,
            InvoiceStatus invoiceStatus,
            PaymentStatus paymentStatus,
            Long staffId
    ) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("""
                    SELECT count(i.id)
                    FROM Invoice i
                    JOIN i.user u
                    LEFT JOIN i.customer c
                    LEFT JOIN Payment p ON p.id = (
                        SELECT max(p2.id) FROM Payment p2 WHERE p2.invoice.id = i.id
                    )
                    WHERE 1=1
                    """);
            applyFilters(hql, keyword, fromDate, toDate, invoiceStatus, paymentStatus, staffId);
            Query<Long> query = session.createQuery(hql.toString(), Long.class);
            bindFilters(query, keyword, fromDate, toDate, invoiceStatus, paymentStatus, staffId);
            return query.uniqueResultOptional().orElse(0L);
        } catch (Exception e) {
            log.error("Error counting invoices", e);
            throw e;
        }
    }

    @Override
    public Optional<Invoice> findDetailHeaderById(Long invoiceId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
                    SELECT i
                    FROM Invoice i
                    JOIN FETCH i.user
                    LEFT JOIN FETCH i.customer
                    WHERE i.id = :invoiceId
                    """;
            return Optional.ofNullable(session.createQuery(hql, Invoice.class)
                    .setParameter("invoiceId", invoiceId)
                    .uniqueResult());
        } catch (Exception e) {
            log.error("Error finding invoice detail header by id={}", invoiceId, e);
            throw e;
        }
    }

    @Override
    public List<TicketLineDTO> findTicketLinesByInvoiceId(Long invoiceId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
                    SELECT new com.f3cinema.app.dto.transaction.TicketLineDTO(
                        m.title,
                        r.name,
                        s.startTime,
                        concat(seat.rowChar, seat.number),
                        t.finalPrice
                    )
                    FROM Ticket t
                    JOIN t.showtime s
                    JOIN s.movie m
                    JOIN s.room r
                    JOIN t.seat seat
                    WHERE t.invoice.id = :invoiceId
                    ORDER BY s.startTime ASC, seat.rowChar ASC, seat.number ASC
                    """;
            return session.createQuery(hql, TicketLineDTO.class)
                    .setParameter("invoiceId", invoiceId)
                    .list();
        }
    }

    @Override
    public List<SnackLineDTO> findSnackLinesByInvoiceId(Long invoiceId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
                    SELECT new com.f3cinema.app.dto.transaction.SnackLineDTO(
                        p.name,
                        ii.quantity,
                        ii.unitPrice,
                        (ii.unitPrice * ii.quantity)
                    )
                    FROM InvoiceItem ii
                    JOIN ii.product p
                    WHERE ii.invoice.id = :invoiceId
                    ORDER BY ii.id ASC
                    """;
            return session.createQuery(hql, SnackLineDTO.class)
                    .setParameter("invoiceId", invoiceId)
                    .list();
        }
    }

    @Override
    public List<PaymentLineDTO> findPaymentLinesByInvoiceId(Long invoiceId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
                    SELECT new com.f3cinema.app.dto.transaction.PaymentLineDTO(
                        p.id, p.amount, p.method, p.status, p.transactionId
                    )
                    FROM Payment p
                    WHERE p.invoice.id = :invoiceId
                    ORDER BY p.id DESC
                    """;
            return session.createQuery(hql, PaymentLineDTO.class)
                    .setParameter("invoiceId", invoiceId)
                    .list();
        }
    }

    @Override
    public void updateInvoiceStatus(Long invoiceId, InvoiceStatus status) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            MutationQuery query = session.createMutationQuery("""
                    UPDATE Invoice i
                    SET i.status = :status
                    WHERE i.id = :invoiceId
                    """);
            query.setParameter("status", status);
            query.setParameter("invoiceId", invoiceId);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            log.error("Error updating invoice status for id={}", invoiceId, e);
            throw e;
        }
    }

    @Override
    public void updatePaymentsStatusByInvoiceId(Long invoiceId, PaymentStatus status) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            MutationQuery query = session.createMutationQuery("""
                    UPDATE Payment p
                    SET p.status = :status
                    WHERE p.invoice.id = :invoiceId
                    """);
            query.setParameter("status", status);
            query.setParameter("invoiceId", invoiceId);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            log.error("Error updating payment statuses for invoice id={}", invoiceId, e);
            throw e;
        }
    }

    private void applyFilters(
            StringBuilder hql,
            String keyword,
            LocalDate fromDate,
            LocalDate toDate,
            InvoiceStatus invoiceStatus,
            PaymentStatus paymentStatus,
            Long staffId
    ) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            hql.append(" AND (");
            hql.append(" lower(c.fullName) LIKE :kw");
            hql.append(" OR c.phone LIKE :kwPhone");
            hql.append(" OR cast(i.id as string) LIKE :kwPhone");
            hql.append(" )");
        }
        if (fromDate != null) {
            hql.append(" AND i.createdAt >= :fromDateTime");
        }
        if (toDate != null) {
            hql.append(" AND i.createdAt < :toDateExclusive");
        }
        if (invoiceStatus != null) {
            hql.append(" AND i.status = :invoiceStatus");
        }
        if (paymentStatus != null) {
            hql.append(" AND p.status = :paymentStatus");
        }
        if (staffId != null) {
            hql.append(" AND u.id = :staffId");
        }
    }

    private void bindFilters(
            Query<?> query,
            String keyword,
            LocalDate fromDate,
            LocalDate toDate,
            InvoiceStatus invoiceStatus,
            PaymentStatus paymentStatus,
            Long staffId
    ) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            String clean = keyword.trim();
            query.setParameter("kw", "%" + clean.toLowerCase(Locale.ROOT) + "%");
            query.setParameter("kwPhone", "%" + clean + "%");
        }
        if (fromDate != null) {
            query.setParameter("fromDateTime", fromDate.atStartOfDay());
        }
        if (toDate != null) {
            query.setParameter("toDateExclusive", toDate.plusDays(1).atStartOfDay());
        }
        if (invoiceStatus != null) {
            query.setParameter("invoiceStatus", invoiceStatus);
        }
        if (paymentStatus != null) {
            query.setParameter("paymentStatus", paymentStatus);
        }
        if (staffId != null) {
            query.setParameter("staffId", staffId);
        }
    }
}
