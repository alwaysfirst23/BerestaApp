package org.example.demo.infrastructure;

import java.sql.*;

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
                + " password TEXT NOT NULL,\n"
                + " display_name TEXT,\n"  // Добавленное поле для имени пользователя
                + " avatar_url TEXT\n"     // Добавленное поле для ссылки на аватар
                + ");";

        try (Connection conn = DatabaseConnector.authConnect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Таблица пользователей создана/проверена.");

            // Добавляем новые колонки, если их нет (для случаев обновления)
            addColumnIfNotExists(conn, "users", "display_name", "TEXT");
            addColumnIfNotExists(conn, "users", "avatar_url", "TEXT");

        } catch (SQLException e) {
            System.out.println("Ошибка инициализации БД: " + e.getMessage());
        }
    }

    /**
     * Вспомогательный метод для добавления колонки, если она не существует
     */
    private static void addColumnIfNotExists(Connection conn, String table, String column, String type) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(null, null, table, column)) {
            if (!rs.next()) {
                String sql = String.format("ALTER TABLE %s ADD COLUMN %s %s", table, column, type);
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                    System.out.println("Добавлена колонка " + column + " в таблицу " + table);
                }
            }
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
                + " is_done INTEGER DEFAULT 0,\n" // 0 - не выполнено, 1 - выполнено
                + " project TEXT\n" // Новый столбец для хранения проекта
                + ");";

        try (Connection conn = DatabaseConnector.taskConnect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Таблица задач создана.");

            // Проверяем существование столбца project (для случаев, когда таблица уже существует)
            try {
                stmt.execute("ALTER TABLE tasks ADD COLUMN project TEXT");
                System.out.println("Столбец project добавлен.");
            } catch (SQLException e) {
                // Игнорируем ошибку, если столбец уже существует
                System.out.println("Столбец project уже существует или не может быть добавлен: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при создании таблицы задач: " + e.getMessage());
        }
    }

    /**
     * Инициализация таблицы для хранения связей между задачами и подзадачами
     */
    public static void subtasksInitialize() {
        String sql = "CREATE TABLE IF NOT EXISTS task_relations (\n"
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + " parent_id INTEGER NOT NULL,\n"
                + " child_id INTEGER NOT NULL,\n"
                + " FOREIGN KEY (parent_id) REFERENCES tasks(id),\n"
                + " FOREIGN KEY (child_id) REFERENCES tasks(id),\n"
                + " UNIQUE(parent_id, child_id)\n"
                + ");";

        try (Connection conn = DatabaseConnector.taskConnect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Таблица связей задач создана.");
        } catch (SQLException e) {
            System.out.println("Ошибка при создании таблицы связей задач: " + e.getMessage());
        }
    }
}
