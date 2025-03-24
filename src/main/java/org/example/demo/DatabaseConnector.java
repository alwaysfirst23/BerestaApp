package org.example.demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Класс осуществляет подключение к базе данных
 */

public class DatabaseConnector {
    private static final String DB_URL = "jdbc:sqlite:user_database.db";

    /**
     * Подключается к базе данных
     * @return объект класса Connection
     */
    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
            System.out.println("Подключение к базе данных успешно установлено.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
}
