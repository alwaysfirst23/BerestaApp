package org.example.demo.services;

import org.example.demo.domain.Task;
import org.example.demo.infrastructure.DatabaseSubtaskRepository;
import org.example.demo.infrastructure.DatabaseTaskRepository;

import java.sql.Connection;
import java.util.List;

public class TaskService {
    private final DatabaseTaskRepository taskRepo;
    private final DatabaseSubtaskRepository subtaskRepo;

    public TaskService(Connection connection) {
        this.taskRepo = new DatabaseTaskRepository(connection);
        this.subtaskRepo = new DatabaseSubtaskRepository(connection);
    }

    // === Основные методы для задач ===
    public List<Task> findAllTasks() {
        return taskRepo.findAll();
    }

    public Task findTaskById(int taskId) {
        return taskRepo.findById(taskId);
    }

    public void createTask(Task task, Integer parentId) {
        taskRepo.save(task);  // Сохраняем задачу

        if (parentId != null) {
            subtaskRepo.addSubtask(parentId, task.getId());  // Добавляем связь
        }
    }

    public void updateTask(Task task) {
        taskRepo.update(task);
    }

    public void deleteTask(int taskId) {
        // Удаляем все связи задачи (как родителя и как подзадачи)
        subtaskRepo.removeAllLinks(taskId);
        // Удаляем саму задачу
        taskRepo.delete(taskId);
    }

    // === Методы для подзадач ===
    public List<Integer> getSubtasks(int parentId) {
        return subtaskRepo.findSubtasks(parentId);
    }

    public void addSubtask(int parentId, int childId) {
        // Проверяем, что задачи существуют
        if (taskRepo.findById(parentId) == null || taskRepo.findById(childId) == null) {
            throw new IllegalArgumentException("Родитель или подзадача не найдены");
        }
        subtaskRepo.addSubtask(parentId, childId);
    }

    public void removeSubtask(int parentId, int childId) {
        subtaskRepo.removeSubtask(parentId, childId);
    }

    public boolean isSubtask(int taskId) {
        return subtaskRepo.isSubtask(taskId);
    }
}
