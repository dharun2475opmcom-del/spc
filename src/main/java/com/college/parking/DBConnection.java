package com.college.parking;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Opens a connection to the Smart Parking MySQL database.
 *
 * Configure DB_URL, DB_USER, and DB_PASSWORD as environment variables.
 * Never commit database credentials to source control.
 */
public final class DBConnection {
    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        if (isBlank(url) || isBlank(user) || password == null) {
            throw new SQLException(
                "Database is not configured. Set DB_URL, DB_USER, and DB_PASSWORD environment variables."
            );
        }

        return DriverManager.getConnection(url, user, password);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
