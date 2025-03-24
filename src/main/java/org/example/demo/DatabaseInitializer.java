package org.example.demo;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Создаёт и инициализирует новую базу данных
 */
public class DatabaseInitializer {
    /**
     * Инициализация базы данных
     */
    public static void initialize() {
        String sql = "CREATE TABLE IF NOT EXISTS users (\n"
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + " username TEXT NOT NULL UNIQUE,\n"
                + " password TEXT NOT NULL\n"
                + ");";

        try (Connection conn = DatabaseConnector.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Таблица пользователей создана.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
