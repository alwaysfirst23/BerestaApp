package org.example.demo.infrastructure;

import org.example.demo.domain.Task;
import java.util.List;
import java.time.LocalDate;

public interface TaskRepository {
    List<Task> findAll();

    /**
     * Сохраняет задачу
     * @param task объект класса Task, задача
     */
    void save(Task task);

    /**
     * Обновляет задачу
     * @param task объект класса Task, задача
     */
    void update(Task task);

    /**
     * Удаление задачи из списка
     * @param taskId id задачи
     */
    void delete(int taskId);

    /**
     * Найти задачи для конкретной колонки
     * @param project название колонки
     * @return список задач
     */
    List<Task> findByProject(String project);

    /**
     * Изменяет статус задачи
     * @param taskId id задачи
     * @param isDone статус - true, если выполнено, иначе - false
     */
    void updateTaskStatus(int taskId, boolean isDone);
}
