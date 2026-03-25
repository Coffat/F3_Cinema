package com.f3cinema.app.config;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import lombok.extern.log4j.Log4j2;

/**
 * Thread-safe Singleton Utility for Hibernate SessionFactory using MetadataSources.
 */
@Log4j2
public class HibernateUtil {
    private static SessionFactory sessionFactory;

    private HibernateUtil() {}

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            synchronized (HibernateUtil.class) {
                if (sessionFactory == null) {
                    try {
                        StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                                .configure("hibernate.cfg.xml").build();

                        Metadata metadata = new MetadataSources(serviceRegistry).getMetadataBuilder().build();

                        sessionFactory = metadata.getSessionFactoryBuilder().build();
                        log.info("Hibernate SessionFactory initialized successfully.");
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
    }
}
