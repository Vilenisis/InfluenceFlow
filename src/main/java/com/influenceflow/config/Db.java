package com.influenceflow.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public final class Db {
    private static volatile HikariDataSource dataSource;

    private Db() {
    }

    public static DataSource getDataSource() {
        if (dataSource == null) {
            synchronized (Db.class) {
                if (dataSource == null) {
                    HikariConfig config = new HikariConfig();
                    config.setJdbcUrl(getEnvOrThrow("DB_URL"));
                    config.setUsername(getEnvOrThrow("DB_USER"));
                    config.setPassword(getEnvOrThrow("DB_PASS"));
                    config.setMaximumPoolSize(10);
                    config.setPoolName("InfluenceFlowPool");
                    dataSource = new HikariDataSource(config);
                }
            }
        }
        return dataSource;
    }

    private static String getEnvOrThrow(String name) {
        String value = System.getenv(name);
        if (value == null) {
            throw new IllegalStateException("Environment variable " + name + " is not set");
        }
        value = value.trim();
        if (value.isEmpty()) {
            throw new IllegalStateException("Environment variable " + name + " is blank");
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Environment variable " + name + " is not set");
        }
        return value;
    }
}
