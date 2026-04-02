package com.f3cinema.app.config;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Thread-safe Singleton Utility for Hibernate SessionFactory with HikariCP connection pooling.
 */
public class HibernateUtil {
    private static final Logger log = LogManager.getLogger(HibernateUtil.class);

    private static SessionFactory sessionFactory;

    private HibernateUtil() {}

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            synchronized (HibernateUtil.class) {
                if (sessionFactory == null) {
                    try {
                        Map<String, Object> settings = new HashMap<>();
                        settings.put("hibernate.connection.datasource", DataSourceConfig.getDataSource());
                        settings.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
                        settings.put("hibernate.show_sql", true);
                        settings.put("hibernate.format_sql", true);
                        settings.put("hibernate.hbm2ddl.auto", "update");
                        
                        StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                                .applySettings(settings)
                                .build();

                        MetadataSources metadataSources = new MetadataSources(serviceRegistry);
                        
                        metadataSources.addAnnotatedClass(com.f3cinema.app.entity.User.class);
                        metadataSources.addAnnotatedClass(com.f3cinema.app.entity.Customer.class);
                        metadataSources.addAnnotatedClass(com.f3cinema.app.entity.Movie.class);
                        metadataSources.addAnnotatedClass(com.f3cinema.app.entity.Genre.class);
                        metadataSources.addAnnotatedClass(com.f3cinema.app.entity.Room.class);
                        metadataSources.addAnnotatedClass(com.f3cinema.app.entity.Seat.class);
                        metadataSources.addAnnotatedClass(com.f3cinema.app.entity.Showtime.class);
                        metadataSources.addAnnotatedClass(com.f3cinema.app.entity.Product.class);
                        metadataSources.addAnnotatedClass(com.f3cinema.app.entity.Inventory.class);
                        metadataSources.addAnnotatedClass(com.f3cinema.app.entity.StockReceipt.class);
                        metadataSources.addAnnotatedClass(com.f3cinema.app.entity.StockReceiptItem.class);
                        metadataSources.addAnnotatedClass(com.f3cinema.app.entity.Promotion.class);
                        metadataSources.addAnnotatedClass(com.f3cinema.app.entity.Invoice.class);
                        metadataSources.addAnnotatedClass(com.f3cinema.app.entity.Ticket.class);
                        metadataSources.addAnnotatedClass(com.f3cinema.app.entity.InvoiceItem.class);
                        metadataSources.addAnnotatedClass(com.f3cinema.app.entity.Payment.class);
                        metadataSources.addAnnotatedClass(com.f3cinema.app.entity.Voucher.class);

                        Metadata metadata = metadataSources.getMetadataBuilder().build();
                        sessionFactory = metadata.getSessionFactoryBuilder().build();
                        
                        log.info("Hibernate SessionFactory initialized successfully with HikariCP.");
                    } catch (Exception e) {
                        log.error("Initial SessionFactory creation failed.", e);
                        throw new ExceptionInInitializerError(e);
                    }
                }
            }
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
            log.info("Hibernate SessionFactory closed.");
        }
        DataSourceConfig.shutdown();
    }
}
