package com.f3cinema.app.service.discount;

import com.f3cinema.app.entity.Voucher;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Strategy for fixed amount discount.
 * Example: 50,000đ off, cannot exceed order amount.
 */
public class FixedAmountDiscountStrategy implements DiscountStrategy {

    @Override
    public BigDecimal calculateDiscount(Voucher voucher, BigDecimal orderAmount, Map<String, Object> context) {
        BigDecimal discountAmount = voucher.getDiscountAmount();
        
        if (discountAmount.compareTo(orderAmount) > 0) {
            return orderAmount;
        }
        
        return discountAmount;
    }

    @Override
    public void validate(Voucher voucher, BigDecimal orderAmount, Map<String, Object> context) {
        if (voucher.getDiscountAmount() == null || voucher.getDiscountAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền giảm không hợp lệ.");
        }
    }
}
