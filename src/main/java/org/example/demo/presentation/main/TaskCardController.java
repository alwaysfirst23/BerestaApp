package org.example.demo.presentation.main;

import javafx.fxml.FXML;
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

    public void setTask(Task task) {
        titleLabel.setText("Название: " + task.getTitle());
        descriptionLabel.setText("Описание: " + task.getDescription());
        deadlineLabel.setText("Дедлайн: " + task.getDeadline());
        workerLabel.setText("Исполнитель: " + task.getWorker());
        priorityLabel.setText("Приоритет: " + task.getPriority());
        statusLabel.setText("Статус: " + (task.isDone() ? "Выполнено" : "В работе"));
    }
}
