package org.example.demo.presentation.main;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import lombok.Setter;
import org.example.demo.domain.Task;
import org.example.demo.domain.exceptions.IncorrectTask;
import org.example.demo.infrastructure.DatabaseTaskRepository;
import org.example.demo.presentation.TaskDialog;
import org.example.demo.services.TaskService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Контроллер колонки проекта для управления задачами.
 */
public class ProjectColumnController {
    @FXML private VBox projectColumn;
    @FXML private Label projectNameLabel;
    @FXML private VBox tasksContainer;
    @FXML private Button addTaskButton;

    private String projectName;
    @Setter
    private TaskService taskService; // Заменяем DatabaseTaskRepository на TaskService

    /**
     * Устанавливает название проекта.
     *
     * @param name название проекта
     */
    public void setProjectName(String name) {
        this.projectName = name;
        projectNameLabel.setText(name);
    }

    /**
     * Обрабатывает добавление новой задачи.
     */
    @FXML
    public void handleAddTask() {
        TaskDialog dialog = new TaskDialog(projectName);
        Optional<Task> result = dialog.showAndWait();

        result.ifPresent(task -> {
            try {
                taskService.createTask(task, null); // null - значит это не подзадача
                addTask(task);
            } catch (Exception e) {
                showErrorAlert("Ошибка сохранения задачи", e.getMessage());
            }
        });
    }

    /**
     * Добавляет задачу в контейнер.
     *
     * @param task задача для добавления
     */
    public void addTask(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/task_card.fxml"));
            VBox taskCard = loader.load();
            TaskCardController controller = loader.getController();

            // Проверяем, является ли задача подзадачей
            boolean isSubtask = taskService.isSubtask(task.getId());

            controller.setTask(
                    task,
                    taskService,
                    isSubtask,
                    this::refreshColumn
            );

            // Добавляем в контейнер только если это не подзадача
            if (!isSubtask) {
                tasksContainer.getChildren().add(taskCard);
            }
        } catch (IOException e) {
            showErrorAlert("Ошибка", "Не удалось загрузить карточку задачи");
        }
    }

    /**
     * Обновляет колонку проекта.
     */
    private void refreshColumn() {
        tasksContainer.getChildren().clear();
        loadTasksForProject();
    }

    /**
     * Загружает задачи для текущего проекта.
     */
    private void loadTasksForProject() {
        try {
            // Загружаем только родительские задачи для этого проекта
            List<Task> parentTasks = taskService.findAllTasks().stream()
                    .filter(task -> projectName.equals(task.getProject()))
                    .filter(task -> !taskService.isSubtask(task.getId()))
                    .toList();

            parentTasks.forEach(this::addTask);
        } catch (Exception e) {
            showErrorAlert("Ошибка", "Не удалось загрузить задачи");
        }
    }

    /**
     * Показывает алерт с ошибкой.
     *
     * @param title заголовок алерта
     * @param message сообщение
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}