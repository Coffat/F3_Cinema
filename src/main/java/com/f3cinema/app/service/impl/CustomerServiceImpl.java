package com.f3cinema.app.service.impl;

import com.f3cinema.app.config.HibernateUtil;
import com.f3cinema.app.dto.customer.CustomerListItemDTO;
import com.f3cinema.app.dto.customer.CustomerSearchRequest;
import com.f3cinema.app.dto.customer.CustomerSearchResult;
import com.f3cinema.app.entity.Customer;
import com.f3cinema.app.repository.CustomerRepository;
import com.f3cinema.app.repository.CustomerRepositoryImpl;
import com.f3cinema.app.service.CustomerService;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * CustomerServiceImpl - Business logic for customer management and loyalty program.
 */
@Log4j2
public class CustomerServiceImpl implements CustomerService {

    private static CustomerServiceImpl instance;
    private final CustomerRepository customerRepository;

    private CustomerServiceImpl() {
        this.customerRepository = new CustomerRepositoryImpl();
    }

    public static synchronized CustomerServiceImpl getInstance() {
        if (instance == null) {
            instance = new CustomerServiceImpl();
        }
        return instance;
    }

    @Override
    public Customer findOrCreateByPhone(String phone, String name) {
        log.info("Looking up customer by phone: {}", phone);
        
        Optional<Customer> existing = customerRepository.findByPhone(phone);
        if (existing.isPresent()) {
            log.info("Customer found: {}", existing.get().getFullName());
            return existing.get();
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên khách hàng không được để trống cho khách hàng mới!");
        }

        log.info("Creating new customer: {} - {}", phone, name);
        Customer newCustomer = Customer.builder()
                .phone(phone)
                .fullName(name.trim())
                .points(0)
                .build();

        return customerRepository.save(newCustomer);
    }

    @Override
    public Customer updatePoints(Long customerId, int pointsChange) {
        log.info("Updating points for customer ID {}: {} points", customerId, pointsChange);

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Customer customer = session.get(Customer.class, customerId);
            if (customer == null) {
                throw new RuntimeException("Không tìm thấy khách hàng ID: " + customerId);
            }

            int currentPoints = customer.getPoints() != null ? customer.getPoints() : 0;
            int newPoints = currentPoints + pointsChange;

            if (newPoints < 0) {
                throw new IllegalArgumentException("Không đủ điểm để thực hiện giao dịch!");
            }

            customer.setPoints(newPoints);
            session.merge(customer);

            tx.commit();
            log.info("Points updated successfully. New balance: {}", newPoints);
            return customer;

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            log.error("Error updating customer points: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi cập nhật điểm: " + e.getMessage());
        }
    }

    @Override
    public int calculatePointsFromAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return amount.divide(BigDecimal.valueOf(1000), 0, java.math.RoundingMode.DOWN).intValue();
    }

    @Override
    public CustomerSearchResult searchCustomers(CustomerSearchRequest request) {
        CustomerSearchRequest safe = request == null
                ? new CustomerSearchRequest(null, null, null, 0, 30, null)
                : request;

        List<CustomerListItemDTO> items = customerRepository.search(
                safe.query(),
                safe.minPoints(),
                safe.maxPoints(),
                safe.offset(),
                safe.limit(),
                safe.sort()
        );
        long total = customerRepository.countSearch(safe.query(), safe.minPoints(), safe.maxPoints());
        return new CustomerSearchResult(items, total, safe.offset(), safe.limit());
    }
}
