package org.example.demo.presentation.main;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.example.demo.domain.Task;

public class ProjectColumnController {
    @FXML private VBox projectColumn;
    @FXML private Label projectNameLabel;
    @FXML private VBox tasksContainer;

    public void setProjectName(String name) {
        projectNameLabel.setText(name);
    }

    public void addTask(Task task) {
        tasksContainer.getChildren().add(createTaskCard(task));
    }

    private Node createTaskCard(Task task) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));

        card.getChildren().addAll(
                new Label("Название: " + task.getTitle()),
                new Label("Описание: " + task.getDescription()),
                new Label("Дедлайн: " + task.getDeadline()),
                new Label("Исполнитель: " + task.getWorker()),
                new Label("Приоритет: " + task.getPriority()),
                new Label("Статус: " + (task.isDone() ? "Выполнено" : "В работе"))
        );

        return card;
    }
}