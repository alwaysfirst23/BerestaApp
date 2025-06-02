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

    /**
     * Инициализация таблицы для хранения статистики по задачам
     * (новая версия с динамическими проектами)
     */
    public static void statsInitialize() {
        // Основная таблица статистики (без привязки к конкретным проектам)
        String statsTableSql = "CREATE TABLE IF NOT EXISTS task_stats (\n"
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + " timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,\n"
                + " total_tasks INTEGER NOT NULL,\n"
                + " completed_tasks INTEGER NOT NULL,\n"
                + " overdue_tasks INTEGER NOT NULL,\n"
                + " in_progress_tasks INTEGER NOT NULL,\n"
                + " high_priority_tasks INTEGER NOT NULL,\n"
                + " medium_priority_tasks INTEGER NOT NULL,\n"
                + " low_priority_tasks INTEGER NOT NULL,\n"
                + " project_completion REAL NOT NULL\n"
                + ");";

        // Таблица статистики по проектам (динамическая)
        String projectStatsSql = "CREATE TABLE IF NOT EXISTS project_stats (\n"
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + " stats_id INTEGER NOT NULL,\n"
                + " project_name TEXT NOT NULL,\n"
                + " task_count INTEGER NOT NULL,\n"
                + " completed_count INTEGER NOT NULL,\n"
                + " FOREIGN KEY (stats_id) REFERENCES task_stats(id),\n"
                + " UNIQUE(stats_id, project_name)\n"
                + ");";

        try (Connection conn = DatabaseConnector.taskConnect();
             Statement stmt = conn.createStatement()) {

            // Создаем таблицы
            stmt.execute(statsTableSql);
            stmt.execute(projectStatsSql);
            System.out.println("Таблицы статистики созданы.");

            // Создаем триггеры для автоматического обновления
            createDynamicStatsTriggers(conn);

            // Инициализируем первое значение статистики
            updateDynamicStats(conn);

        } catch (SQLException e) {
            System.out.println("Ошибка при создании таблиц статистики: " + e.getMessage());
        }
    }

    /**
     * Создаёт триггеры для динамического обновления статистики
     */
    private static void createDynamicStatsTriggers(Connection conn) throws SQLException {
        // Общий триггер для всех операций
        String triggerSql = """
            CREATE TRIGGER IF NOT EXISTS update_dynamic_stats
            AFTER INSERT OR UPDATE OR DELETE ON tasks
            BEGIN
                -- Вставляем основную статистику
                INSERT INTO task_stats (
                    total_tasks, completed_tasks, overdue_tasks, in_progress_tasks,
                    high_priority_tasks, medium_priority_tasks, low_priority_tasks,
                    project_completion
                )
                SELECT 
                    COUNT(*) as total_tasks,
                    SUM(CASE WHEN is_done = 1 THEN 1 ELSE 0 END) as completed_tasks,
                    SUM(CASE WHEN is_done = 0 AND deadline < DATE('now') THEN 1 ELSE 0 END) as overdue_tasks,
                    SUM(CASE WHEN is_done = 0 AND deadline >= DATE('now') THEN 1 ELSE 0 END) as in_progress_tasks,
                    SUM(CASE WHEN priority = 3 THEN 1 ELSE 0 END) as high_priority_tasks,
                    SUM(CASE WHEN priority = 2 THEN 1 ELSE 0 END) as medium_priority_tasks,
                    SUM(CASE WHEN priority = 1 THEN 1 ELSE 0 END) as low_priority_tasks,
                    CASE WHEN COUNT(*) > 0 THEN ROUND(SUM(is_done) * 100.0 / COUNT(*), 2) ELSE 0 END as project_completion
                FROM tasks;
                
                -- Получаем ID только что вставленной статистики
                SET @last_stats_id = last_insert_rowid();
                
                -- Вставляем статистику по проектам
                INSERT INTO project_stats (stats_id, project_name, task_count, completed_count)
                SELECT 
                    @last_stats_id,
                    project as project_name,
                    COUNT(*) as task_count,
                    SUM(CASE WHEN is_done = 1 THEN 1 ELSE 0 END) as completed_count
                FROM tasks
                WHERE project IS NOT NULL
                GROUP BY project;
            END;
            """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(triggerSql);
            System.out.println("Триггер для динамической статистики создан.");
        }
    }

    /**
     * Обновляет статистику вручную (динамическая версия)
     */
    private static void updateDynamicStats(Connection conn) throws SQLException {
        String updateSql = """
            -- Вставляем основную статистику
            INSERT INTO task_stats (
                total_tasks, completed_tasks, overdue_tasks, in_progress_tasks,
                high_priority_tasks, medium_priority_tasks, low_priority_tasks,
                project_completion
            )
            SELECT 
                COUNT(*) as total_tasks,
                SUM(CASE WHEN is_done = 1 THEN 1 ELSE 0 END) as completed_tasks,
                SUM(CASE WHEN is_done = 0 AND deadline < DATE('now') THEN 1 ELSE 0 END) as overdue_tasks,
                SUM(CASE WHEN is_done = 0 AND deadline >= DATE('now') THEN 1 ELSE 0 END) as in_progress_tasks,
                SUM(CASE WHEN priority = 3 THEN 1 ELSE 0 END) as high_priority_tasks,
                SUM(CASE WHEN priority = 2 THEN 1 ELSE 0 END) as medium_priority_tasks,
                SUM(CASE WHEN priority = 1 THEN 1 ELSE 0 END) as low_priority_tasks,
                CASE WHEN COUNT(*) > 0 THEN ROUND(SUM(is_done) * 100.0 / COUNT(*), 2) ELSE 0 END as project_completion
            FROM tasks;
            
            -- Вставляем статистику по проектам
            INSERT INTO project_stats (stats_id, project_name, task_count, completed_count)
            SELECT 
                last_insert_rowid(),
                project as project_name,
                COUNT(*) as task_count,
                SUM(CASE WHEN is_done = 1 THEN 1 ELSE 0 END) as completed_count
            FROM tasks
            WHERE project IS NOT NULL
            GROUP BY project;
            """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(updateSql);
            System.out.println("Динамическая статистика загружена.");
        }
    }

    /**
     * Инициализация только таблицы task_stats и её компонентов
     */
    public static void statsInitializeBasic() {
        // Основная таблица статистики
        String statsTableSql = "CREATE TABLE IF NOT EXISTS task_stats (\n"
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + " timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,\n"
                + " total_tasks INTEGER NOT NULL,\n"
                + " completed_tasks INTEGER NOT NULL,\n"
                + " overdue_tasks INTEGER NOT NULL,\n"
                + " in_progress_tasks INTEGER NOT NULL,\n"
                + " high_priority_tasks INTEGER NOT NULL,\n"
                + " medium_priority_tasks INTEGER NOT NULL,\n"
                + " low_priority_tasks INTEGER NOT NULL,\n"
                + " project_completion REAL NOT NULL\n"
                + ");";

        try (Connection conn = DatabaseConnector.taskConnect();
             Statement stmt = conn.createStatement()) {

            // Создаем основную таблицу статистики
            stmt.execute(statsTableSql);
            System.out.println("Таблица task_stats создана/проверена.");

            // Создаем упрощённый триггер для обновления статистики
            createBasicStatsTrigger(conn);

            // Инициализируем первое значение статистики
            updateBasicStats(conn);

        } catch (SQLException e) {
            System.out.println("Ошибка при инициализации task_stats: " + e.getMessage());
        }
    }

    /**
     * Создаёт базовый триггер для обновления статистики
     */
    private static void createBasicStatsTrigger(Connection conn) throws SQLException {
        // Создаем отдельные триггеры для INSERT, UPDATE и DELETE
        String[] triggerSQLs = {
                // Триггер для INSERT
                """
        CREATE TRIGGER IF NOT EXISTS update_stats_after_insert
        AFTER INSERT ON tasks
        BEGIN
            INSERT INTO task_stats (
                total_tasks, completed_tasks, overdue_tasks, in_progress_tasks,
                high_priority_tasks, medium_priority_tasks, low_priority_tasks,
                project_completion
            )
            SELECT 
                COUNT(*),
                SUM(CASE WHEN is_done = 1 THEN 1 ELSE 0 END),
                SUM(CASE WHEN is_done = 0 AND deadline < DATE('now') THEN 1 ELSE 0 END),
                SUM(CASE WHEN is_done = 0 AND deadline >= DATE('now') THEN 1 ELSE 0 END),
                SUM(CASE WHEN priority = 3 THEN 1 ELSE 0 END),
                SUM(CASE WHEN priority = 2 THEN 1 ELSE 0 END),
                SUM(CASE WHEN priority = 1 THEN 1 ELSE 0 END),
                CASE WHEN COUNT(*) > 0 THEN ROUND(SUM(is_done) * 100.0 / COUNT(*), 2) ELSE 0 END
            FROM tasks;
        END;
        """,

                // Триггер для UPDATE
                """
        CREATE TRIGGER IF NOT EXISTS update_stats_after_update
        AFTER UPDATE ON tasks
        BEGIN
            INSERT INTO task_stats (
                total_tasks, completed_tasks, overdue_tasks, in_progress_tasks,
                high_priority_tasks, medium_priority_tasks, low_priority_tasks,
                project_completion
            )
            SELECT 
                COUNT(*),
                SUM(CASE WHEN is_done = 1 THEN 1 ELSE 0 END),
                SUM(CASE WHEN is_done = 0 AND deadline < DATE('now') THEN 1 ELSE 0 END),
                SUM(CASE WHEN is_done = 0 AND deadline >= DATE('now') THEN 1 ELSE 0 END),
                SUM(CASE WHEN priority = 3 THEN 1 ELSE 0 END),
                SUM(CASE WHEN priority = 2 THEN 1 ELSE 0 END),
                SUM(CASE WHEN priority = 1 THEN 1 ELSE 0 END),
                CASE WHEN COUNT(*) > 0 THEN ROUND(SUM(is_done) * 100.0 / COUNT(*), 2) ELSE 0 END
            FROM tasks;
        END;
        """,

                // Триггер для DELETE
                """
        CREATE TRIGGER IF NOT EXISTS update_stats_after_delete
        AFTER DELETE ON tasks
        BEGIN
            INSERT INTO task_stats (
                total_tasks, completed_tasks, overdue_tasks, in_progress_tasks,
                high_priority_tasks, medium_priority_tasks, low_priority_tasks,
                project_completion
            )
            SELECT 
                COUNT(*),
                SUM(CASE WHEN is_done = 1 THEN 1 ELSE 0 END),
                SUM(CASE WHEN is_done = 0 AND deadline < DATE('now') THEN 1 ELSE 0 END),
                SUM(CASE WHEN is_done = 0 AND deadline >= DATE('now') THEN 1 ELSE 0 END),
                SUM(CASE WHEN priority = 3 THEN 1 ELSE 0 END),
                SUM(CASE WHEN priority = 2 THEN 1 ELSE 0 END),
                SUM(CASE WHEN priority = 1 THEN 1 ELSE 0 END),
                CASE WHEN COUNT(*) > 0 THEN ROUND(SUM(is_done) * 100.0 / COUNT(*), 2) ELSE 0 END
            FROM tasks;
        END;
        """
        };

        try (Statement stmt = conn.createStatement()) {
            for (String sql : triggerSQLs) {
                stmt.execute(sql);
            }
            System.out.println("Триггеры для статистики созданы.");
        }
    }

    /**
     * Обновляет базовую статистику вручную
     */
    private static void updateBasicStats(Connection conn) throws SQLException {
        String updateSql = """
        INSERT INTO task_stats (
            total_tasks, completed_tasks, overdue_tasks, in_progress_tasks,
            high_priority_tasks, medium_priority_tasks, low_priority_tasks,
            project_completion
        )
        SELECT
            COUNT(*),
            SUM(CASE WHEN is_done = 1 THEN 1 ELSE 0 END),
            SUM(CASE WHEN is_done = 0 AND deadline < DATE('now') THEN 1 ELSE 0 END),
            SUM(CASE WHEN is_done = 0 AND deadline >= DATE('now') THEN 1 ELSE 0 END),
            SUM(CASE WHEN priority = 3 THEN 1 ELSE 0 END),
            SUM(CASE WHEN priority = 2 THEN 1 ELSE 0 END),
            SUM(CASE WHEN priority = 1 THEN 1 ELSE 0 END),
            CASE WHEN COUNT(*) > 0 THEN ROUND(SUM(is_done) * 100.0 / COUNT(*), 2) ELSE 0 END
        FROM tasks;
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(updateSql);
            System.out.println("Базовая статистика загружена.");
        }
    }
}
