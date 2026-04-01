package com.f3cinema.app.repository;

import com.f3cinema.app.config.HibernateUtil;
import com.f3cinema.app.dto.customer.CustomerListItemDTO;
import com.f3cinema.app.dto.customer.CustomerSort;
import com.f3cinema.app.entity.Customer;
import lombok.extern.log4j.Log4j2;
import org.hibernate.query.Query;
import org.hibernate.Session;

import java.util.List;
import java.util.Locale;
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

    @Override
    public List<CustomerListItemDTO> search(String query, Integer minPoints, Integer maxPoints, int offset, int limit, CustomerSort sort) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("""
                    SELECT new com.f3cinema.app.dto.customer.CustomerListItemDTO(
                        c.id, c.fullName, c.phone, coalesce(c.points, 0)
                    )
                    FROM Customer c
                    WHERE 1=1
                    """);

            boolean hasQuery = query != null && !query.trim().isEmpty();
            if (hasQuery) {
                hql.append(" AND (lower(c.fullName) LIKE :q OR c.phone LIKE :qPhone)");
            }
            if (minPoints != null) {
                hql.append(" AND coalesce(c.points, 0) >= :minPoints");
            }
            if (maxPoints != null) {
                hql.append(" AND coalesce(c.points, 0) <= :maxPoints");
            }

            hql.append(sort == CustomerSort.POINTS_DESC
                    ? " ORDER BY coalesce(c.points, 0) DESC, c.fullName ASC"
                    : " ORDER BY c.fullName ASC");

            Query<CustomerListItemDTO> q = session.createQuery(hql.toString(), CustomerListItemDTO.class);
            applySearchParams(q, query, hasQuery, minPoints, maxPoints);
            return q.setFirstResult(Math.max(0, offset))
                    .setMaxResults(limit <= 0 ? 30 : limit)
                    .list();
        } catch (Exception e) {
            log.error("Error searching customers: query={}, min={}, max={}", query, minPoints, maxPoints, e);
            throw e;
        }
    }

    @Override
    public long countSearch(String query, Integer minPoints, Integer maxPoints) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("""
                    SELECT count(c.id)
                    FROM Customer c
                    WHERE 1=1
                    """);

            boolean hasQuery = query != null && !query.trim().isEmpty();
            if (hasQuery) {
                hql.append(" AND (lower(c.fullName) LIKE :q OR c.phone LIKE :qPhone)");
            }
            if (minPoints != null) {
                hql.append(" AND coalesce(c.points, 0) >= :minPoints");
            }
            if (maxPoints != null) {
                hql.append(" AND coalesce(c.points, 0) <= :maxPoints");
            }

            Query<Long> q = session.createQuery(hql.toString(), Long.class);
            applySearchParams(q, query, hasQuery, minPoints, maxPoints);
            return q.uniqueResultOptional().orElse(0L);
        } catch (Exception e) {
            log.error("Error counting customer search: query={}, min={}, max={}", query, minPoints, maxPoints, e);
            throw e;
        }
    }

    private void applySearchParams(Query<?> q, String query, boolean hasQuery, Integer minPoints, Integer maxPoints) {
        if (hasQuery) {
            String normalized = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
            String phoneQuery = "%" + query.trim() + "%";
            q.setParameter("q", normalized);
            q.setParameter("qPhone", phoneQuery);
        }
        if (minPoints != null) {
            q.setParameter("minPoints", minPoints);
        }
        if (maxPoints != null) {
            q.setParameter("maxPoints", maxPoints);
        }
    }
}
