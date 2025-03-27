package org.example.demo;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * В классе реализована логика работы добавления, удаления, редактирования задачи.
 * Обращаю внимание на то, что класс работает с базой данных
 */
public class TaskBase {
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Создаёт новую записать в базе данных
     * @param title заголовок
     * @param description описание
     * @param priority приоритет (1-4)
     * @param date дедлайн
     * @param worker исполнитель
     * @throws IncorrectTask если некорректно заполнены обязательные поля
     * @throws SQLException если нет связи с базой
     */
    public void createTask(String title, String description, int priority, LocalDate date, String worker, String project) throws IncorrectTask, SQLException {
        if (worker == null || worker.trim().isEmpty()) {
            worker = "Я";
        }

        String sql = "INSERT INTO tasks(title, description, priority, deadline, worker, project) VALUES(?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnector.taskConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.setInt(3, priority);
            pstmt.setString(4, date != null ? date.format(DateTimeFormatter.ISO_LOCAL_DATE) : null);
            pstmt.setString(5, worker);
            pstmt.setString(6, project);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка создания задачи: " + e.getMessage());
        }
    }

    /**
     * Удаляет задачу с указанным номером из базы
     * @param index номер задачи
     */
    public void removeTask(int index) {
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = DatabaseConnector.taskConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, index);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка удаления задачи: " + e.getMessage());
        }
    }

    /**
     * Редактирует задачу по индексу, обновляя запись в базе
     * @param index номер задачи
     * @param title новый заголовок
     * @param description новое описание
     * @param priority новый приоритет
     * @param deadline новый дедлайн
     * @param worker новый исполнитель
     */
    public void editTask(int index, String title, String description, int priority, LocalDate deadline, String worker, String project, boolean isDone) {
        String sql = "UPDATE tasks SET title=?, description=?, priority=?, deadline=?, "
                + "worker=?, project=?, is_done=? WHERE id=?";

        try (Connection conn = DatabaseConnector.taskConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.setInt(3, priority);
            pstmt.setString(4, deadline != null ? deadline.format(DateTimeFormatter.ISO_LOCAL_DATE) : null);
            pstmt.setString(5, worker);
            pstmt.setInt(6, index);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка редактирования задачи: " + e.getMessage());
        }
    }

    /**
     * Возвращает список всех задач
     * @return список задач
     */
    public String printAllTasks() {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT * FROM tasks";

        try (Connection conn = DatabaseConnector.taskConnect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                sb.append(rs.getInt("id"))
                        .append(". [")
                        .append(rs.getInt("is_done") == 1 ? "✓" : " ")
                        .append("] ")
                        .append(rs.getString("title"))
                        .append("\n   Описание: ")
                        .append(rs.getString("description"))
                        .append("\n   Приоритет: ")
                        .append(rs.getInt("priority"))
                        .append("\n   Дедлайн: ")
                        .append(rs.getString("deadline") != null
                                ? LocalDate.parse(rs.getString("deadline")).format(dateFormatter)
                                : "нет")
                        .append("\n   Исполнитель: ")
                        .append(rs.getString("worker").isEmpty() ? "я" : rs.getString("worker"))
                        .append("\n\n");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения задач: " + e.getMessage());
        }

        return sb.toString().isEmpty() ? "Список задач пуст" : sb.toString();
    }

    public void updateTaskStatus(int taskId, boolean isDone) throws SQLException {
        String sql = "UPDATE tasks SET is_done = ? WHERE id = ?";

        try (Connection conn = DatabaseConnector.taskConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, isDone ? 1 : 0);
            pstmt.setInt(2, taskId);
            pstmt.executeUpdate();
        }
    }
}