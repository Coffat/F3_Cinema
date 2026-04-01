package com.f3cinema.app.service.discount;

import com.f3cinema.app.entity.Voucher;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Strategy for combo-specific discount.
 * Example: 30% off on combo items only (bắp nước combos).
 */
public class ComboDiscountStrategy implements DiscountStrategy {

    @Override
    public BigDecimal calculateDiscount(Voucher voucher, BigDecimal orderAmount, Map<String, Object> context) {
        @SuppressWarnings("unchecked")
        Map<Long, Integer> snacksCart = (Map<Long, Integer>) context.get("snacksCart");
        
        if (snacksCart == null || snacksCart.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal comboTotal = (BigDecimal) context.getOrDefault("comboTotal", BigDecimal.ZERO);
        
        if (comboTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal discountPercent = voucher.getDiscountPercent();
        BigDecimal discount = comboTotal
                .multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        if (voucher.getMaxDiscount() != null && discount.compareTo(voucher.getMaxDiscount()) > 0) {
            discount = voucher.getMaxDiscount();
        }
        
        return discount;
    }

    @Override
    public void validate(Voucher voucher, BigDecimal orderAmount, Map<String, Object> context) {
        if (voucher.getDiscountPercent() == null || voucher.getDiscountPercent().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Phần trăm giảm không hợp lệ.");
        }
        
        @SuppressWarnings("unchecked")
        Map<Long, Integer> snacksCart = (Map<Long, Integer>) context.get("snacksCart");
        
        if (snacksCart == null || snacksCart.isEmpty()) {
            throw new IllegalArgumentException("Voucher này chỉ áp dụng cho combo bắp nước.");
        }
        
        BigDecimal comboTotal = (BigDecimal) context.getOrDefault("comboTotal", BigDecimal.ZERO);
        if (comboTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Không có combo nào trong đơn hàng.");
        }
    }
}
