package com.f3cinema.app.repository;

import com.f3cinema.app.entity.Customer;

import java.util.Optional;

/**
 * CustomerRepository interface for Customer data access.
 */
public interface CustomerRepository extends BaseRepository<Customer, Long> {
    Optional<Customer> findByPhone(String phone);
}
