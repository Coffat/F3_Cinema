package com.f3cinema.app.service.discount;

import com.f3cinema.app.entity.Voucher;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Strategy interface for different voucher discount calculations.
 * Each voucher type (PERCENTAGE, FIXED_AMOUNT, etc.) implements its own strategy.
 */
public interface DiscountStrategy {
    
    /**
     * Calculate the discount amount based on the voucher and order context.
     * 
     * @param voucher The voucher being applied
     * @param orderAmount The total order amount before discount
     * @param context Additional context (seatCount, snacksCart, etc.)
     * @return The calculated discount amount
     */
    BigDecimal calculateDiscount(Voucher voucher, BigDecimal orderAmount, Map<String, Object> context);
    
    /**
     * Validate that the voucher can be applied to this order.
     * Throws IllegalArgumentException if validation fails.
     * 
     * @param voucher The voucher being applied
     * @param orderAmount The total order amount before discount
     * @param context Additional context for validation
     */
    void validate(Voucher voucher, BigDecimal orderAmount, Map<String, Object> context);
}
