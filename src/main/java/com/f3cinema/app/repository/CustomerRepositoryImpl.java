package com.f3cinema.app.repository;

import com.f3cinema.app.config.HibernateUtil;
import com.f3cinema.app.entity.Customer;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;

import java.util.Optional;

/**
 * CustomerRepositoryImpl - Hibernate 6 implementation.
 * Handles customer lookup by phone for loyalty program.
 */
@Log4j2
public class CustomerRepositoryImpl extends BaseRepositoryImpl<Customer, Long> implements CustomerRepository {

    public CustomerRepositoryImpl() {
        super(Customer.class);
    }

    @Override
    public Optional<Customer> findByPhone(String phone) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Customer c WHERE c.phone = :phone";
            Customer customer = session.createQuery(hql, Customer.class)
                    .setParameter("phone", phone)
                    .uniqueResult();
            return Optional.ofNullable(customer);
        } catch (Exception e) {
            log.error("Error finding customer by phone: {}", phone, e);
            throw e;
        }
    }
}
