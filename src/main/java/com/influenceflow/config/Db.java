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
                    String rawUrl = getEnvOrThrow("DB_URL");
                    config.setJdbcUrl(normalizeJdbcUrl(rawUrl));
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
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalStateException("Environment variable " + name + " is blank");
        }
        return trimmed;
    }

    private static String normalizeJdbcUrl(String url) {
        if (url.startsWith("jdbc:")) {
            return url;
        }
        String lower = url.toLowerCase();
        if (lower.startsWith("postgresql+")) {
            int idx = url.indexOf("://");
            if (idx > 0 && idx + 3 < url.length()) {
                return "jdbc:postgresql://" + url.substring(idx + 3);
            }
        }
        if (lower.startsWith("postgresql://")) {
            return "jdbc:postgresql://" + url.substring("postgresql://".length());
        }
        if (lower.startsWith("postgres://")) {
            return "jdbc:postgresql://" + url.substring("postgres://".length());
        }
        return url;
    }
}
