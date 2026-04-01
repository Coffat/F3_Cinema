package com.f3cinema.app.service.discount;

import com.f3cinema.app.entity.Voucher;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Strategy for Buy X Get Y discount (applies to seats).
 * Example: Buy 2 seats, get 1 free. The cheapest seat among selected seats is discounted.
 */
public class BuyXGetYDiscountStrategy implements DiscountStrategy {

    @Override
    public BigDecimal calculateDiscount(Voucher voucher, BigDecimal orderAmount, Map<String, Object> context) {
        Integer seatCount = (Integer) context.get("seatCount");
        BigDecimal seatTotal = (BigDecimal) context.getOrDefault("seatTotal", BigDecimal.ZERO);
        
        if (seatCount == null || seatCount < voucher.getBuyQuantity()) {
            return BigDecimal.ZERO;
        }
        
        int freeSeats = (seatCount / voucher.getBuyQuantity()) * voucher.getGetQuantity();
        
        BigDecimal avgSeatPrice = seatTotal.divide(BigDecimal.valueOf(seatCount), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal discount = avgSeatPrice.multiply(BigDecimal.valueOf(freeSeats));
        
        return discount.min(seatTotal);
    }

    @Override
    public void validate(Voucher voucher, BigDecimal orderAmount, Map<String, Object> context) {
        if (voucher.getBuyQuantity() == null || voucher.getBuyQuantity() <= 0) {
            throw new IllegalArgumentException("Số lượng mua không hợp lệ.");
        }
        
        if (voucher.getGetQuantity() == null || voucher.getGetQuantity() <= 0) {
            throw new IllegalArgumentException("Số lượng tặng không hợp lệ.");
        }
        
        Integer seatCount = (Integer) context.get("seatCount");
        if (seatCount == null || seatCount < voucher.getBuyQuantity()) {
            throw new IllegalArgumentException(
                String.format("Cần mua tối thiểu %d ghế để áp dụng voucher này.", voucher.getBuyQuantity())
            );
        }
    }
}
