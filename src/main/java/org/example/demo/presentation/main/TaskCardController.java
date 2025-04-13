package org.example.demo.presentation.main;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.example.demo.domain.Task;

public class TaskCardController {
    @FXML private VBox taskCard;
    @FXML private Label titleLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label deadlineLabel;
    @FXML private Label workerLabel;
    @FXML private Label priorityLabel;
    @FXML private Label statusLabel;
    @FXML private Button markAsDoneButton;

    private Task task;
    private Runnable onMarkAsDone;

    public void setTask(Task task, Runnable onMarkAsDone) {
        this.task = task;
        this.onMarkAsDone = onMarkAsDone;

        // Обновляем UI
        titleLabel.setText("Название: " + task.getTitle());
        descriptionLabel.setText("Описание: " + task.getDescription());
        deadlineLabel.setText("Дедлайн: " + task.getDeadline());
        workerLabel.setText("Исполнитель: " + task.getWorker());
        priorityLabel.setText("Приоритет: " + task.getPriority());
        statusLabel.setText("Статус: " + (task.isDone() ? "Выполнено" : "В работе"));

        updateButtonState();
    }

    @FXML
    private void handleMarkAsDone() {
        if (!task.isDone()) {
            task.setDone(true);
            updateButtonState();
            if (onMarkAsDone != null) {
                onMarkAsDone.run();
            }
        }
    }

    private void updateButtonState() {
        markAsDoneButton.setDisable(task.isDone());
        markAsDoneButton.setText(task.isDone() ? "Выполнено" : "Отметить выполненной");
        markAsDoneButton.setStyle(task.isDone()
                ? "-fx-background-color: #cccccc;"
                : "-fx-background-color: #4CAF50; -fx-text-fill: white;");
    }
}
