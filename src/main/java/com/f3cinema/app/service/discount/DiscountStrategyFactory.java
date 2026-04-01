package com.f3cinema.app.service.discount;

import com.f3cinema.app.entity.enums.VoucherType;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating appropriate discount strategy based on voucher type.
 * Implements Strategy Pattern for voucher discount calculations.
 */
public class DiscountStrategyFactory {
    
    private static final Map<VoucherType, DiscountStrategy> strategies = new HashMap<>();
    
    static {
        strategies.put(VoucherType.PERCENTAGE, new PercentageDiscountStrategy());
        strategies.put(VoucherType.FIXED_AMOUNT, new FixedAmountDiscountStrategy());
        strategies.put(VoucherType.BUY_X_GET_Y, new BuyXGetYDiscountStrategy());
        strategies.put(VoucherType.COMBO_DISCOUNT, new ComboDiscountStrategy());
    }
    
    /**
     * Get the appropriate discount strategy for the given voucher type.
     * 
     * @param type The voucher type
     * @return The corresponding discount strategy
     * @throws IllegalArgumentException if voucher type is not supported
     */
    public static DiscountStrategy getStrategy(VoucherType type) {
        DiscountStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported voucher type: " + type);
        }
        return strategy;
    }
}
