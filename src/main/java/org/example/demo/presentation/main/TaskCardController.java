package org.example.demo.presentation.main;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;
import org.example.demo.domain.Task;
import org.example.demo.presentation.TaskDialog;
import org.example.demo.services.TaskService;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TaskCardController {
    @FXML private VBox mainContainer;
    @FXML private VBox subtasksContainer;
    @FXML private Label titleLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label deadlineLabel;
    @FXML private Label workerLabel;
    @FXML private Label priorityLabel;
    @FXML private Label statusLabel;
    @FXML private Button markAsDoneButton;
    @FXML private Button addSubtaskButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button toggleSubtasksButton;
    @FXML private ImageView toggleIcon;
    @FXML private Circle statusCircle;
    @FXML private ImageView checkIcon;

    private Task task;
    private TaskService taskService;
    private boolean isSubtask;
    private Runnable refreshCallback;

    public void setTask(Task task, TaskService taskService, boolean isSubtask, Runnable refreshCallback) {
        this.task = task;
        this.taskService = taskService;
        this.isSubtask = isSubtask;
        this.refreshCallback = refreshCallback;

        updateUI();
        setupSubtasksSection();
    }

    private void updateUI() {
        titleLabel.setText(task.getTitle());
        descriptionLabel.setText(task.getDescription());
        deadlineLabel.setText("Дедлайн: " + (task.getDeadline() != null ? task.getDeadline() : "Нет"));
        workerLabel.setText("Исполнитель: " + task.getWorker());
        priorityLabel.setText("Приоритет: " + task.getPriority());
        statusLabel.setText("Статус: " + (task.isDone() ? "Выполнено" : "В работе"));

        if (isSubtask) {
            mainContainer.getStyleClass().add("subtask");
            mainContainer.setStyle("-fx-border-color: #08D4D4; -fx-border-width: 0 0 0 2;");
        }

        if (task.isDone()) {
            statusCircle.setVisible(false);  // Скрываем кружок
            checkIcon.setVisible(true);      // Показываем иконку
            markAsDoneButton.setDisable(true);
        } else {
            statusCircle.setVisible(true);   // Показываем кружок
            checkIcon.setVisible(false);     // Скрываем иконку
            markAsDoneButton.setDisable(false);
        }
    }

    private void setupSubtasksSection() {
        if (isSubtask) {
            toggleSubtasksButton.setVisible(false);
            return;
        }

        toggleSubtasksButton.setOnAction(e -> toggleSubtasksVisibility());
        loadSubtasks();
    }

    private void loadSubtasks() {
        subtasksContainer.getChildren().clear();

        try {
            List<Task> subtasks = taskService.findSubtasks(task.getId());

            if (!subtasks.isEmpty()) {
                subtasks.forEach(this::addSubtaskCard);
                subtasksContainer.setVisible(true);
            } else {
                subtasksContainer.setVisible(false);
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки подзадач: " + e.getMessage());
            subtasksContainer.setVisible(false);
        }
    }

    private void addSubtaskCard(Task subtask) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/task_card.fxml"));
            VBox subtaskCard = loader.load();
            TaskCardController controller = loader.getController();

            controller.setTask(subtask, taskService, true, this::loadSubtasks);

            subtasksContainer.getChildren().add(subtaskCard);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void toggleSubtasksVisibility() {
        if (subtasksContainer.isVisible()) {
            collapseSubtasks();
        } else {
            expandSubtasks();
        }
    }

    private void expandSubtasks() {
        subtasksContainer.setVisible(true);
        subtasksContainer.setManaged(true); // Возвращаем в расчет layout

        // Анимация раскрытия
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), subtasksContainer);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        // Восстанавливаем исходную высоту
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), subtasksContainer);
        scaleIn.setFromY(0);
        scaleIn.setToY(1);

        new ParallelTransition(fadeIn, scaleIn).play();
        toggleIcon.setRotate(0); // Возвращаем иконку в исходное положение
    }

    private void collapseSubtasks() {
        // Запоминаем текущую высоту перед сворачиванием
        subtasksContainer.setUserData(subtasksContainer.getHeight());

        // Анимация сворачивания
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), subtasksContainer);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        // Параллельно анимируем высоту
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), subtasksContainer);
        scaleOut.setToY(0);

        ParallelTransition transition = new ParallelTransition(fadeOut, scaleOut);
        transition.setOnFinished(e -> {
            subtasksContainer.setVisible(false);
            subtasksContainer.setManaged(false); // Важно! Убираем из расчета layout
        });
        transition.play();

        toggleIcon.setRotate(-90); // Поворачиваем иконку
    }

    @FXML
    private void handleMarkAsDone() {
        if (!task.isDone()) {
            task.setDone(true);
            try {
                taskService.updateTask(task);
                // Мгновенное переключение
                statusCircle.setVisible(false);
                checkIcon.setVisible(true);
                markAsDoneButton.setDisable(true);

                // Показываем уведомление
                showCompletionNotification();

                if (refreshCallback != null) {
                    refreshCallback.run();
                }
            } catch (Exception e) {
                showAlert("Ошибка", "Не удалось обновить задачу");
            }
        }
    }

    private void showCompletionNotification() {
        // Создаем кастомное уведомление
        Alert notification = new Alert(Alert.AlertType.INFORMATION);
        notification.setTitle("Задача выполнена");
        notification.setHeaderText(null);
        notification.setContentText("Спасибо! За эту задачу вы получите вознаграждение после проверки тимлидом");

        // Добавляем иконку
        ImageView icon = new ImageView(new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/checkmark.png"))));
        icon.setFitHeight(50);
        icon.setFitWidth(50);
        notification.setGraphic(icon);

        // Настраиваем стиль
        notification.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles.css").toExternalForm());
        notification.getDialogPane().getStyleClass().add("reward-notification");

        notification.show();
    }

    @FXML
    private void handleAddSubtask() {
        TaskDialog dialog = new TaskDialog(task.getProject());
        Optional<Task> result = dialog.showAndWait();

        result.ifPresent(subtask -> {
            try {
                taskService.createTask(subtask, task.getId());
                loadSubtasks();
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
            } catch (Exception e) {
                showAlert("Ошибка", "Не удалось создать подзадачу");
            }
        });
    }

    @FXML
    private void handleEdit() {
        TaskDialog dialog = new TaskDialog(task.getProject(), task);
        Optional<Task> result = dialog.showAndWait();

        result.ifPresent(updatedTask -> {
            try {
                taskService.updateTask(updatedTask);
                updateUI();
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
            } catch (Exception e) {
                showAlert("Ошибка", "Не удалось обновить задачу");
            }
        });
    }

    @FXML
    private void handleDelete() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Подтверждение удаления");
        confirmation.setHeaderText("Удалить задачу \"" + task.getTitle() + "\"?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                taskService.deleteTask(task.getId());
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
            } catch (Exception e) {
                showAlert("Ошибка", "Не удалось удалить задачу");
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}