package org.example.demo.services;

import org.example.demo.domain.ProjectStats;
import org.example.demo.domain.TaskStats;
import org.example.demo.infrastructure.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class StatsDao {
    /**
     * Получает основную статистику
     */
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

    /**
     * Получает статистику по проектам (название -> количество задач)
     */
    public static Map<String, Integer> getProjectStats() {
        String sql = """
            SELECT project_name, task_count 
            FROM project_stats ps
            JOIN task_stats ts ON ps.stats_id = ts.id
            WHERE ts.timestamp = (SELECT MAX(timestamp) FROM task_stats)
            ORDER BY task_count DESC
            """;

        Map<String, Integer> stats = new LinkedHashMap<>();

        try (Connection conn = DatabaseConnector.taskConnect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                stats.put(rs.getString("project_name"), rs.getInt("task_count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }
}
