package org.example.demo;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Создаёт и инициализирует базы данных
 */
public class DatabaseInitializer {
    /**
     * Инициализация базы данных для хранения учётных данных пользователя
     */
    public static void authInitialize() {
        String sql = "CREATE TABLE IF NOT EXISTS users (\n"
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + " username TEXT NOT NULL UNIQUE,\n"
                + " password TEXT NOT NULL\n"
                + ");";

        try (Connection conn = DatabaseConnector.authConnect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Таблица пользователей создана.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Инициализация базы данных для хранения списка задач
     */
    public static void taskInitialize() {
        String sql = "CREATE TABLE IF NOT EXISTS tasks (\n"
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + " title TEXT NOT NULL,\n"
                + " description TEXT,\n"
                + " priority INTEGER NOT NULL,\n"
                + " deadline TEXT,\n"
                + " worker TEXT NOT NULL,\n"
                + " is_done INTEGER DEFAULT 0\n" // 0 - не выполнено, 1 - выполнено
                + ");";

        try (Connection conn = DatabaseConnector.taskConnect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Таблица задач создана.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
