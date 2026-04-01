package com.f3cinema.app.repository;

import com.f3cinema.app.entity.Voucher;
import com.f3cinema.app.entity.enums.VoucherStatus;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Voucher entity.
 * Extends BaseRepository for generic CRUD operations.
 */
public interface VoucherRepository extends BaseRepository<Voucher, Long> {

    /**
     * Find a voucher by its unique code.
     */
    Optional<Voucher> findByCode(String code);

    /**
     * Find all vouchers by status.
     */
    List<Voucher> findByStatus(VoucherStatus status);

    /**
     * Search vouchers by code or description (case-insensitive).
     */
    List<Voucher> searchVouchers(String keyword);

    /**
     * Find all active vouchers that are currently valid.
     */
    List<Voucher> findActiveVouchers();
}
