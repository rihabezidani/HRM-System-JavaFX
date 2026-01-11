package com.rh.javafx.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Hibernate Utility class with a SessionFactory singleton
 */
public class HibernateUtil {

    private static SessionFactory sessionFactory;

    static {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            Configuration configuration = new Configuration().configure("hibernate.cfg.xml");

            // Override database connection properties from environment variables if present
            String dbHost = System.getenv("DB_HOST");
            String dbPort = System.getenv("DB_PORT");
            String dbName = System.getenv("DB_NAME");
            String dbUser = System.getenv("DB_USER");
            String dbPassword = System.getenv("DB_PASSWORD");

            if (dbHost != null && dbPort != null && dbName != null) {
                String jdbcUrl = String.format(
                    "jdbc:mysql://%s:%s/%s?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                    dbHost, dbPort, dbName
                );
                configuration.setProperty("hibernate.connection.url", jdbcUrl);
                System.out.println("Using database URL from environment: " + jdbcUrl);
            }

            if (dbUser != null) {
                configuration.setProperty("hibernate.connection.username", dbUser);
            }

            if (dbPassword != null) {
                configuration.setProperty("hibernate.connection.password", dbPassword);
            }

            sessionFactory = configuration.buildSessionFactory();
            System.out.println("SessionFactory créée avec succès!");
        } catch (Throwable ex) {
            System.err.println("Échec de création de SessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Get the SessionFactory instance
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Shutdown the SessionFactory
     */
    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            System.out.println("SessionFactory fermée.");
        }
    }
}
