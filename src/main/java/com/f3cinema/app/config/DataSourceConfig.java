package com.f3cinema.app.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.log4j.Log4j2;

import javax.sql.DataSource;

/**
 * HikariCP DataSource Configuration for optimized connection pooling.
 * Provides a singleton DataSource instance for the application.
 */
@Log4j2
public class DataSourceConfig {
    private static HikariDataSource dataSource;

    private DataSourceConfig() {}

    public static DataSource getDataSource() {
        if (dataSource == null) {
            synchronized (DataSourceConfig.class) {
                if (dataSource == null) {
                    try {
                        HikariConfig config = new HikariConfig();
                        
                        config.setJdbcUrl("jdbc:mysql://localhost:3307/f3_cinema?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8&connectionCollation=utf8mb4_unicode_ci");
                        config.setUsername("root");
                        config.setPassword("123456");
                        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
                        
                        config.setMinimumIdle(5);
                        config.setMaximumPoolSize(20);
                        config.setConnectionTimeout(30000);
                        config.setIdleTimeout(600000);
                        config.setMaxLifetime(1800000);
                        config.setPoolName("F3CinemaHikariPool");
                        
                        config.addDataSourceProperty("cachePrepStmts", "true");
                        config.addDataSourceProperty("prepStmtCacheSize", "250");
                        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                        config.addDataSourceProperty("useServerPrepStmts", "true");
                        config.addDataSourceProperty("useLocalSessionState", "true");
                        config.addDataSourceProperty("rewriteBatchedStatements", "true");
                        config.addDataSourceProperty("cacheResultSetMetadata", "true");
                        config.addDataSourceProperty("cacheServerConfiguration", "true");
                        config.addDataSourceProperty("elideSetAutoCommits", "true");
                        config.addDataSourceProperty("maintainTimeStats", "false");
                        
                        dataSource = new HikariDataSource(config);
                        log.info("HikariCP DataSource initialized successfully with pool size {}-{}", 
                                config.getMinimumIdle(), config.getMaximumPoolSize());
                    } catch (Exception e) {
                        log.error("Failed to initialize HikariCP DataSource", e);
                        throw new ExceptionInInitializerError(e);
                    }
                }
            }
        }
        return dataSource;
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            log.info("HikariCP DataSource closed.");
        }
    }
}
