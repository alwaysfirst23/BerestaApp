package org.example.demo.presentation.main;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.example.demo.domain.Task;

import java.util.Optional;

public class TaskCardController {
    @FXML public Label titleLabel;
    @FXML public Label descriptionLabel;
    @FXML public Label deadlineLabel;
    @FXML public Label workerLabel;
    @FXML public Label priorityLabel;
    @FXML public Label statusLabel;
    @FXML private Button markAsDoneButton;
    @FXML private Button addSubtaskButton;
    @FXML private Button deleteButton;
    @FXML private Button editButton;

    private Task task;
    private Runnable onTaskDone;
    private Runnable onDeleteTask;
    private Runnable onEditTask;
    private Runnable onAddSubtask;

    public void setTask(Task task, Runnable onTaskDone, Runnable onDeleteTask,
                        Runnable onEditTask, Runnable onAddSubtask) {
        this.task = task;
        this.onTaskDone = onTaskDone;
        this.onDeleteTask = onDeleteTask;
        this.onEditTask = onEditTask;
        this.onAddSubtask = onAddSubtask;
        updateUI();
    }

    void updateUI() {
        titleLabel.setText(task.getTitle());
        descriptionLabel.setText(task.getDescription());
        deadlineLabel.setText("Дедлайн: " + task.getDeadline());
        workerLabel.setText("Исполнитель: " + task.getWorker());
        priorityLabel.setText("Приоритет: " + task.getPriority());
        statusLabel.setText("Статус: " + (task.isDone() ? "Выполнено" : "В работе"));

        if (task.isDone()) {
            StackPane graphicContainer = (StackPane) markAsDoneButton.getGraphic();
            ImageView foundIcon = (ImageView) graphicContainer.lookup("#checkIcon");
            foundIcon.setVisible(true);
            markAsDoneButton.setDisable(true);
        } else {
            markAsDoneButton.setDisable(false);
        }
    }

    @FXML
    private void handleMarkAsDone() {
        if (!task.isDone()) {
            task.setDone(true);
            updateUI();
            if (onTaskDone != null) {
                onTaskDone.run();
            }
        }
    }

    @FXML
    private void handleDelete() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Подтверждение удаления");
        confirmation.setHeaderText("Удалить задачу?");
        confirmation.setContentText(task.getTitle());

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (onDeleteTask != null) {
                onDeleteTask.run();
            }
        }
    }

    @FXML
    private void handleEdit() {
        if (onEditTask != null) {
            onEditTask.run();
        }
    }

    @FXML
    private void handleAddSubtask() {
        if (onAddSubtask != null) {
            onAddSubtask.run();
        }
    }
}