package org.example.demo.presentation.main;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
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
            markAsDoneButton.setDisable(true);
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

            // Добавляем соединительную линию
            addConnectionLine(subtaskCard);
            subtasksContainer.getChildren().add(subtaskCard);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addConnectionLine(Node subtaskCard) {
        Path connectionPath = new Path();

        // Создаем изогнутую линию со стрелкой
        MoveTo start = new MoveTo(0, 10);
        CubicCurveTo curve = new CubicCurveTo();
        curve.setControlX1(10); curve.setControlY1(10);
        curve.setControlX2(10); curve.setControlY2(0);
        curve.setX(20); curve.setY(0);

        // Стрелка
        MoveTo arrowStart = new MoveTo(20, 0);
        LineTo arrow1 = new LineTo(15, -5);
        LineTo arrow2 = new LineTo(20, 0);
        LineTo arrow3 = new LineTo(15, 5);

        connectionPath.getElements().addAll(start, curve, arrowStart, arrow1, arrow2, arrow3);
        connectionPath.setStroke(Color.web("#08D4D4"));
        connectionPath.setStrokeWidth(1.5);
        connectionPath.setFill(null);

        StackPane connectionContainer = new StackPane(connectionPath);
        connectionContainer.setAlignment(Pos.TOP_LEFT);
        connectionContainer.setPadding(new Insets(-15, 0, 0, -20));
        subtasksContainer.getChildren().add(connectionContainer);
    }

    private void toggleSubtasksVisibility() {
        if (subtasksContainer.isVisible()) {
            collapseSubtasks();
        } else {
            expandSubtasks();
        }
    }

    private void expandSubtasks() {
        subtasksContainer.setVisible(true);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), subtasksContainer);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
        toggleIcon.setRotate(0);
    }

    private void collapseSubtasks() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), subtasksContainer);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> subtasksContainer.setVisible(false));
        fadeOut.play();
        toggleIcon.setRotate(-90);
    }

    @FXML
    private void handleMarkAsDone() {
        try {
            task.setDone(true);
            taskService.updateTask(task);
            updateUI();
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось обновить задачу");
        }
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