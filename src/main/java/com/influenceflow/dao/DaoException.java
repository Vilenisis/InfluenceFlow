package com.influenceflow.dao;

import java.sql.SQLException;

public class DaoException extends RuntimeException {
    public DaoException(String message, SQLException cause) {
        super(buildSqlMessage(message, cause), cause);
    }

    public DaoException(String message) {
        super(message);
    }

    private static String buildSqlMessage(String message, SQLException cause) {
        StringBuilder builder = new StringBuilder(message);
        builder.append(" (SQLState=").append(cause.getSQLState());
        builder.append(", errorCode=").append(cause.getErrorCode());
        String details = cause.getMessage();
        if (details != null && !details.isBlank()) {
            builder.append(", detail=").append(details.trim());
        }
        builder.append(')');
        return builder.toString();
    }
}
