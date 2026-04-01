package com.f3cinema.app.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Loyalty program tiers for redeeming points to get discount.
 * Example: 500 points = 10% off, 1000 points = 20% off, 2000 points = 30% off.
 */
@Getter
@RequiredArgsConstructor
public enum PointRedemptionTier {
    TIER_500(500, 10, "Giảm 10%"),
    TIER_1000(1000, 20, "Giảm 20%"),
    TIER_2000(2000, 30, "Giảm 30%");

    private final int requiredPoints;
    private final int discountPercent;
    private final String label;

    public static PointRedemptionTier fromPoints(int points) {
        if (points >= TIER_2000.requiredPoints) return TIER_2000;
        if (points >= TIER_1000.requiredPoints) return TIER_1000;
        if (points >= TIER_500.requiredPoints) return TIER_500;
        return null;
    }
}
