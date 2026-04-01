package com.f3cinema.app.repository;

import com.f3cinema.app.dto.customer.CustomerListItemDTO;
import com.f3cinema.app.dto.customer.CustomerSort;
import com.f3cinema.app.entity.Customer;

import java.util.List;
import java.util.Optional;

/**
 * CustomerRepository interface for Customer data access.
 */
public interface CustomerRepository extends BaseRepository<Customer, Long> {
    Optional<Customer> findByPhone(String phone);
    List<CustomerListItemDTO> search(String query, Integer minPoints, Integer maxPoints, int offset, int limit, CustomerSort sort);
    long countSearch(String query, Integer minPoints, Integer maxPoints);
}
