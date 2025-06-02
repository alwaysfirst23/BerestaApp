package org.example.demo.services;

import org.example.demo.domain.ProjectStats;
import org.example.demo.domain.TaskStats;
import org.example.demo.infrastructure.DatabaseConnector;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class StatsDao {
    public static TaskStats getLatestStats() {
        String sql = "SELECT * FROM task_stats ORDER BY timestamp DESC LIMIT 1";

        try (Connection conn = DatabaseConnector.taskConnect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new TaskStats(
                        rs.getInt("total_tasks"),
                        rs.getInt("completed_tasks"),
                        rs.getInt("overdue_tasks"),
                        rs.getInt("in_progress_tasks"),
                        rs.getInt("high_priority_tasks"),
                        rs.getInt("medium_priority_tasks"),
                        rs.getInt("low_priority_tasks"),
                        rs.getDouble("project_completion")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, Integer> getProjectStats() {
        // Используем запрос к основной таблице задач, если нет таблицы project_stats
        String sql = """
            SELECT project, COUNT(*) as task_count 
            FROM tasks 
            WHERE project IS NOT NULL AND project != ''
            GROUP BY project
            ORDER BY task_count DESC
            """;

        Map<String, Integer> stats = new LinkedHashMap<>();

        try (Connection conn = DatabaseConnector.taskConnect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                stats.put(rs.getString("project"), rs.getInt("task_count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    // Метод для ручного обновления статистики
    public static void refreshStatistics() {
        try (Connection conn = DatabaseConnector.taskConnect()) {
            // Вызываем триггер вручную
            String sql = """
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
                stmt.execute(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
