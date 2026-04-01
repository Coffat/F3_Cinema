package com.f3cinema.app.service.discount;

import com.f3cinema.app.entity.Voucher;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Strategy for percentage-based discount.
 * Example: 15% off, capped at max_discount if specified.
 */
public class PercentageDiscountStrategy implements DiscountStrategy {

    @Override
    public BigDecimal calculateDiscount(Voucher voucher, BigDecimal orderAmount, Map<String, Object> context) {
        BigDecimal discountPercent = voucher.getDiscountPercent();
        
        BigDecimal discount = orderAmount
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
            throw new IllegalArgumentException("Voucher phần trăm không hợp lệ.");
        }
    }
}
