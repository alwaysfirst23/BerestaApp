package org.example.demo.infrastructure;

import lombok.AllArgsConstructor;
import org.example.demo.domain.Task;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class DatabaseTaskRepository implements TaskRepository{
    private final Connection connection;

    /**
     * Получает все задачи из базы данных.
     * Конвертирует каждую строку в объект класса Task
     * @return список задач (объектов класса Task)
     */
    @Override
    public List<Task> findAll() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT id, title, description, priority, deadline, worker, is_done, project FROM tasks";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Task task = new Task(
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("priority"),
                        parseDate(rs.getString("deadline")), // Используем строковый парсинг,
                        rs.getString("worker")
                );
                task.setId(rs.getInt("id"));
                task.setDone(rs.getBoolean("is_done"));
                task.setProject(rs.getString("project"));
                tasks.add(task);
            }
            System.out.println("Задачи успешно загружены из базы данных");
        } catch (SQLException e) {
            throw new RuntimeException("Не удаётся загрузить задачи из базы данных", e);
        }
        return tasks;
    }

    /**
     * Вставляет новую запись в базу данных.
     * Устанавливает автоматически сгенерированный ID обратно в объект Task
     * @param task объект класса Task, задача
     */
    @Override
    public void save(Task task) {
        String sql = "INSERT INTO tasks (title, description, priority, deadline, worker, project, is_done) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, task.getTitle());
            pstmt.setString(2, task.getDescription());
            pstmt.setInt(3, task.getPriority());
            pstmt.setDate(4, task.getDeadline() != null ? Date.valueOf(task.getDeadline()) : null);
            pstmt.setString(5, task.getWorker());
            pstmt.setString(6, task.getProject());
            pstmt.setBoolean(7, task.isDone());
            pstmt.executeUpdate();

            // Получаем ID через последний вставленный rowid (специфика SQLite)
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    task.setId(rs.getInt(1));
                }
            }
            System.out.println("Задача успешно добавлена в базу данных");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save task to database", e);
        }
    }

    /**
     * Обновляет все поля задачи по её ID.
     * Проверяет, была ли фактически обновлена запись
     * @param task объект класса Task, задача
     */
    @Override
    public void update(Task task) {
        String sql = "UPDATE tasks SET title = ?, description = ?, priority = ?, " +
                "deadline = ?, worker = ?, project = ?, is_done = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, task.getTitle());
            pstmt.setString(2, task.getDescription());
            pstmt.setInt(3, task.getPriority());
            pstmt.setDate(4, task.getDeadline() != null ? Date.valueOf(task.getDeadline()) : null);
            pstmt.setString(5, task.getWorker());
            pstmt.setString(6, task.getProject());
            pstmt.setBoolean(7, task.isDone());
            pstmt.setInt(8, task.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Не найдено задачи с таким id: " + task.getId());
            }
            System.out.println("Задача успешно обновлена в базе данных");
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось обновить задачу в базе данных", e);
        }
    }

    /**
     * Удаляет задачу по указанному ID.
     * Проверяет, была ли запись фактически удалена
     * @param taskId id задачи
     */
    @Override
    public void delete(int taskId) {
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Не найдено задачи с таким id: " + taskId);
            }
            System.out.println("Задача успешно удалена из базы данных");
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось удалить задачу из базы данных", e);
        }
    }

    /**
     * Ищет задачи по названию колонки
     * @param project название колонки
     * @return список задач, относящихсся к данной колонке
     */
    @Override
    public List<Task> findByProject(String project) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT id, title, description, priority, deadline, worker, is_done FROM tasks " +
                "WHERE project = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, project);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Task task = new Task(
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getInt("priority"),
                            rs.getDate("deadline") != null ? rs.getDate("deadline").toLocalDate() : null,
                            rs.getString("worker")
                    );
                    task.setId(rs.getInt("id"));
                    task.setDone(rs.getBoolean("is_done"));
                    task.setProject(project); // Проект известен из условия
                    tasks.add(task);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось найти задачи в указанной колонке", e);
        }
        return tasks;
    }

    /**
     * Меняет только статус выполнения задачи
     * @param taskId id задачи
     * @param isDone статус - true, если выполнено, иначе - false
     */
    @Override
    public void updateTaskStatus(int taskId, boolean isDone) {
        String sql = "UPDATE tasks SET is_done = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBoolean(1, isDone);
            pstmt.setInt(2, taskId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Не найдено задачи с таким id: " + taskId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось обновить статус", e);
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        // Обработка timestamp (если приходит число)
        if (dateStr.matches("\\d+")) {
            return Instant.ofEpochMilli(Long.parseLong(dateStr))
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        // Обработка строки в формате YYYY-MM-DD
        return LocalDate.parse(dateStr);
    }
}
