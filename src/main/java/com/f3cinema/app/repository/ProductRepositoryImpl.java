package com.f3cinema.app.repository;

import com.f3cinema.app.config.HibernateUtil;
import com.f3cinema.app.entity.Product;
import org.hibernate.Session;
import org.hibernate.Transaction;
import lombok.extern.log4j.Log4j2;
import java.time.LocalDateTime;

@Log4j2
public class ProductRepositoryImpl extends BaseRepositoryImpl<Product, Long> implements ProductRepository {
    
    public ProductRepositoryImpl() {
        super(Product.class);
    }

    @Override
    public void softDelete(Long productId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            log.info("Performing soft delete for Product ID: {}", productId);
            
            String hql = "UPDATE Product p SET p.deletedAt = :now WHERE p.id = :id";
            int updatedEntities = session.createMutationQuery(hql)
                    .setParameter("now", LocalDateTime.now())
                    .setParameter("id", productId)
                    .executeUpdate();
            
            if (updatedEntities == 0) {
                log.warn("No product found to soft delete with ID: {}", productId);
            }
            
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            log.error("Failed to soft delete product with ID: {}", productId, e);
            throw e;
        }
    }
}
