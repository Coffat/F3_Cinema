package com.f3cinema.app.service;

import com.f3cinema.app.entity.Customer;

import java.math.BigDecimal;

/**
 * CustomerService interface for customer management and loyalty program.
 */
public interface CustomerService {
    
    /**
     * Find customer by phone. If not found and name is provided, create new customer.
     * @param phone Customer phone number
     * @param name Customer name (required for new customers)
     * @return Customer entity
     */
    Customer findOrCreateByPhone(String phone, String name);
    
    /**
     * Update customer points (add or subtract).
     * @param customerId Customer ID
     * @param pointsChange Points to add (positive) or subtract (negative)
     * @return Updated customer
     */
    Customer updatePoints(Long customerId, int pointsChange);
    
    /**
     * Calculate loyalty points from purchase amount.
     * Rule: 1000 VND = 1 point
     * @param amount Purchase amount
     * @return Points earned
     */
    int calculatePointsFromAmount(BigDecimal amount);
}
