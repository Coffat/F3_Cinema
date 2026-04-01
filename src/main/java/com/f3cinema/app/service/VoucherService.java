package com.f3cinema.app.service;

import com.f3cinema.app.entity.Voucher;
import com.f3cinema.app.entity.enums.VoucherStatus;
import com.f3cinema.app.entity.enums.VoucherType;
import com.f3cinema.app.repository.VoucherRepository;
import com.f3cinema.app.repository.VoucherRepositoryImpl;
import com.f3cinema.app.service.discount.DiscountStrategy;
import com.f3cinema.app.service.discount.DiscountStrategyFactory;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service for Voucher management and validation.
 */
@Log4j2
public class VoucherService {
    private final VoucherRepository voucherRepository;

    public VoucherService() {
        this.voucherRepository = new VoucherRepositoryImpl();
    }

    /**
     * Create a new voucher with validation.
     */
    public Voucher createVoucher(
            String code,
            String description,
            VoucherType voucherType,
            BigDecimal discountPercent,
            BigDecimal discountAmount,
            BigDecimal maxDiscount,
            BigDecimal minOrderAmount,
            Integer buyQuantity,
            Integer getQuantity,
            String appliesToCategory,
            LocalDateTime validFrom,
            LocalDateTime validUntil,
            Integer usageLimit
    ) {
        validateVoucherInput(code, validFrom, validUntil, voucherType, discountPercent, discountAmount);

        if (voucherRepository.findByCode(code.trim().toUpperCase()).isPresent()) {
            throw new IllegalArgumentException("Mã voucher đã tồn tại.");
        }

        Voucher voucher = Voucher.builder()
                .code(code.trim().toUpperCase())
                .description(description != null ? description.trim() : "")
                .voucherType(voucherType)
                .discountPercent(discountPercent)
                .discountAmount(discountAmount)
                .maxDiscount(maxDiscount)
                .minOrderAmount(minOrderAmount != null ? minOrderAmount : BigDecimal.ZERO)
                .buyQuantity(buyQuantity)
                .getQuantity(getQuantity)
                .appliesToCategory(appliesToCategory)
                .validFrom(validFrom)
                .validUntil(validUntil)
                .usageLimit(usageLimit)
                .usageCount(0)
                .status(VoucherStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        log.info("Creating voucher: {} of type {}", code, voucherType);
        return voucherRepository.save(voucher);
    }

    /**
     * Update an existing voucher.
     */
    public Voucher updateVoucher(
            Long id,
            String code,
            String description,
            VoucherType voucherType,
            BigDecimal discountPercent,
            BigDecimal discountAmount,
            BigDecimal maxDiscount,
            BigDecimal minOrderAmount,
            Integer buyQuantity,
            Integer getQuantity,
            String appliesToCategory,
            LocalDateTime validFrom,
            LocalDateTime validUntil,
            Integer usageLimit,
            VoucherStatus status
    ) {
        Voucher existing = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher."));

        validateVoucherInput(code, validFrom, validUntil, voucherType, discountPercent, discountAmount);

        String normalizedCode = code.trim().toUpperCase();
        voucherRepository.findByCode(normalizedCode).ifPresent(v -> {
            if (!v.getId().equals(id)) {
                throw new IllegalArgumentException("Mã voucher đã tồn tại.");
            }
        });

        existing.setCode(normalizedCode);
        existing.setDescription(description != null ? description.trim() : "");
        existing.setVoucherType(voucherType);
        existing.setDiscountPercent(discountPercent);
        existing.setDiscountAmount(discountAmount);
        existing.setMaxDiscount(maxDiscount);
        existing.setMinOrderAmount(minOrderAmount != null ? minOrderAmount : BigDecimal.ZERO);
        existing.setBuyQuantity(buyQuantity);
        existing.setGetQuantity(getQuantity);
        existing.setAppliesToCategory(appliesToCategory);
        existing.setValidFrom(validFrom);
        existing.setValidUntil(validUntil);
        existing.setUsageLimit(usageLimit);
        existing.setStatus(status);

        log.info("Updating voucher: {} of type {}", code, voucherType);
        return voucherRepository.update(existing);
    }

    /**
     * Delete a voucher (or set to DISABLED).
     */
    public void deleteVoucher(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher."));
        
        voucher.setStatus(VoucherStatus.DISABLED);
        voucherRepository.update(voucher);
        log.info("Disabled voucher: {}", voucher.getCode());
    }

    /**
     * Validate and apply voucher to an order using Strategy Pattern.
     * Returns the discount amount.
     * 
     * @param code Voucher code
     * @param orderAmount Total order amount before discount
     * @param context Context map containing: seatCount, seatTotal, snacksCart, comboTotal, etc.
     */
    public BigDecimal applyVoucher(String code, BigDecimal orderAmount, Map<String, Object> context) {
        Voucher voucher = voucherRepository.findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Mã voucher không tồn tại."));

        validateVoucherStatus(voucher);

        if (orderAmount.compareTo(voucher.getMinOrderAmount()) < 0) {
            throw new IllegalArgumentException(
                String.format("Đơn hàng chưa đủ điều kiện tối thiểu: %,.0fđ", 
                    voucher.getMinOrderAmount().doubleValue())
            );
        }

        DiscountStrategy strategy = DiscountStrategyFactory.getStrategy(voucher.getVoucherType());
        strategy.validate(voucher, orderAmount, context);
        
        BigDecimal discount = strategy.calculateDiscount(voucher, orderAmount, context);

        voucher.setUsageCount(voucher.getUsageCount() + 1);
        voucherRepository.update(voucher);

        log.info("Applied voucher {} (type: {}) to order amount {}, discount: {}", 
            code, voucher.getVoucherType(), orderAmount, discount);
        return discount;
    }

    /**
     * Get all vouchers.
     */
    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    /**
     * Search vouchers by keyword.
     */
    public List<Voucher> searchVouchers(String keyword) {
        return voucherRepository.searchVouchers(keyword);
    }

    /**
     * Get active vouchers.
     */
    public List<Voucher> getActiveVouchers() {
        return voucherRepository.findActiveVouchers();
    }

    private void validateVoucherStatus(Voucher voucher) {
        LocalDateTime now = LocalDateTime.now();

        if (voucher.getStatus() != VoucherStatus.ACTIVE) {
            throw new IllegalArgumentException("Voucher không còn hiệu lực.");
        }

        if (now.isBefore(voucher.getValidFrom())) {
            throw new IllegalArgumentException("Voucher chưa có hiệu lực.");
        }

        if (now.isAfter(voucher.getValidUntil())) {
            throw new IllegalArgumentException("Voucher đã hết hạn.");
        }

        if (voucher.getUsageLimit() != null && voucher.getUsageCount() >= voucher.getUsageLimit()) {
            throw new IllegalArgumentException("Voucher đã hết lượt sử dụng.");
        }
    }

    private void validateVoucherInput(String code, LocalDateTime validFrom, LocalDateTime validUntil,
                                     VoucherType voucherType, BigDecimal discountPercent, BigDecimal discountAmount) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Mã voucher không được để trống.");
        }

        if (code.length() > 50) {
            throw new IllegalArgumentException("Mã voucher không được quá 50 ký tự.");
        }

        if (voucherType == VoucherType.PERCENTAGE || voucherType == VoucherType.COMBO_DISCOUNT) {
            if (discountPercent == null || discountPercent.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Phần trăm giảm giá phải lớn hơn 0.");
            }
            if (discountPercent.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new IllegalArgumentException("Phần trăm giảm giá không được vượt quá 100%.");
            }
        }

        if (voucherType == VoucherType.FIXED_AMOUNT) {
            if (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Số tiền giảm phải lớn hơn 0.");
            }
        }

        if (validFrom == null || validUntil == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và kết thúc không được để trống.");
        }

        if (validFrom.isAfter(validUntil)) {
            throw new IllegalArgumentException("Ngày bắt đầu phải trước ngày kết thúc.");
        }
    }
}
