package com.f3cinema.app.repository;

import com.f3cinema.app.config.HibernateUtil;
import com.f3cinema.app.entity.Voucher;
import com.f3cinema.app.entity.enums.VoucherStatus;
import org.hibernate.Session;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of VoucherRepository with custom query methods.
 */
public class VoucherRepositoryImpl extends BaseRepositoryImpl<Voucher, Long> implements VoucherRepository {

    public VoucherRepositoryImpl() {
        super(Voucher.class);
    }

    @Override
    public Optional<Voucher> findByCode(String code) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Voucher v WHERE v.code = :code", Voucher.class)
                    .setParameter("code", code.trim().toUpperCase())
                    .uniqueResultOptional();
        }
    }

    @Override
    public List<Voucher> findByStatus(VoucherStatus status) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Voucher v WHERE v.status = :status ORDER BY v.createdAt DESC", Voucher.class)
                    .setParameter("status", status)
                    .list();
        }
    }

    @Override
    public List<Voucher> searchVouchers(String keyword) {
        String k = (keyword == null) ? "" : keyword.trim().toLowerCase();
        if (k.isBlank()) {
            return findAll();
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Voucher v " +
                    "WHERE lower(v.code) LIKE :kw " +
                    "OR lower(v.description) LIKE :kw " +
                    "ORDER BY v.createdAt DESC", Voucher.class)
                    .setParameter("kw", "%" + k + "%")
                    .list();
        }
    }

    @Override
    public List<Voucher> findActiveVouchers() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            LocalDateTime now = LocalDateTime.now();
            return session.createQuery(
                    "FROM Voucher v " +
                    "WHERE v.status = :status " +
                    "AND v.validFrom <= :now " +
                    "AND v.validUntil >= :now " +
                    "AND (v.usageLimit IS NULL OR v.usageCount < v.usageLimit) " +
                    "ORDER BY v.createdAt DESC", Voucher.class)
                    .setParameter("status", VoucherStatus.ACTIVE)
                    .setParameter("now", now)
                    .list();
        }
    }

    @Override
    public List<Voucher> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Voucher v ORDER BY v.createdAt DESC", Voucher.class)
                    .list();
        }
    }
}
