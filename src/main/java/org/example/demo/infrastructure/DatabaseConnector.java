package org.example.demo.infrastructure;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Класс осуществляет подключение к базе данных
 */

public class DatabaseConnector {
    private static final String DB_URL_AUTH = "jdbc:sqlite:user_database.db";
    private static final String DB_URL1_TASK = "jdbc:sqlite:tasks.db";

    /**
     * Подключается к базе данных авторизации
     * @return объект класса Connection
     */
    public static Connection authConnect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL_AUTH);
            System.out.println("Подключение к базе данных успешно установлено.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    /**
     * Подключается к базе, где хранятся задачи
     * @return объект класса Connection
     */
    public static Connection taskConnect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL1_TASK);
            System.out.println("Подключение к базе данных успешно установлено.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
}
