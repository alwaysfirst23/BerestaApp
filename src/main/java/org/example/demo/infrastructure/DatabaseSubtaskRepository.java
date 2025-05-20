package org.example.demo.infrastructure;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseSubtaskRepository {
    private final Connection connection;

    public DatabaseSubtaskRepository(Connection connection) {
        this.connection = connection;
    }

    /**
     * Добавляет связь между родительской задачей и подзадачей
     * @param parentId ID родительской задачи
     * @param childId ID подзадачи
     */
    public void addSubtask(int parentId, int childId) {
        String sql = "INSERT INTO task_relations (parent_id, child_id) VALUES (?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, parentId);
            pstmt.setInt(2, childId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось добавить подзадачу", e);
        }
    }

    /**
     * Получает все подзадачи для указанной родительской задачи
     * @param parentId ID родительской задачи
     * @return список ID подзадач
     */
    public List<Integer> findSubtasks(int parentId) {
        List<Integer> subtasks = new ArrayList<>();
        String sql = "SELECT child_id FROM task_relations WHERE parent_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, parentId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    subtasks.add(rs.getInt("child_id"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось получить подзадачи", e);
        }
        return subtasks;
    }

    /**
     * Удаляет связь между родительской задачей и подзадачей
     * @param parentId ID родительской задачи
     * @param childId ID подзадачи
     */
    public void removeSubtask(int parentId, int childId) {
        String sql = "DELETE FROM task_relations WHERE parent_id = ? AND child_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, parentId);
            pstmt.setInt(2, childId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось удалить связь подзадачи", e);
        }
    }

    /**
     * Проверяет, является ли задача подзадачей
     * @param taskId ID задачи
     * @return true если задача является подзадачей
     */
    public boolean isSubtask(int taskId) {
        String sql = "SELECT COUNT(*) FROM task_relations WHERE child_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось проверить статус подзадачи", e);
        }
        return false;
    }

    public void removeAllLinks(int taskId) {
        String sql = "DELETE FROM task_relations WHERE parent_id = ? OR child_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            pstmt.setInt(2, taskId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось удалить связи задачи", e);
        }
    }
}