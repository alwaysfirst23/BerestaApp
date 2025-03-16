package org.example.demo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.io.IOException;

public class TaskApp extends Application {
    private TaskList taskList = new TaskList();
    private TextArea tasksArea = new TextArea();

    @Override
    public void start(Stage primaryStage) {
        // Основная панель
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);

        // Заголовок
        Label title = new Label("Управление задачами");
        root.getChildren().add(title);

        // Создание новой задачи
        Label newTaskLabel = new Label("Создать новую задачу:");
        TextField titleField = new TextField();
        titleField.setPromptText("Заголовок");
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Описание");
        TextField priorityField = new TextField();
        priorityField.setPromptText("Приоритет (1-4)");
        TextField deadlineField = new TextField();
        deadlineField.setPromptText("Дедлайн (ДД/ММ/ГГГГ)");
        TextField workerField = new TextField();
        workerField.setPromptText("Исполнитель");

        Button createTaskButton = new Button("Создать задачу");
        createTaskButton.setOnAction(event -> {
            try {
                taskList.createTask(titleField.getText(), descriptionField.getText(),
                        Integer.parseInt(priorityField.getText()), deadlineField.getText().isEmpty() ? null : LocalDate.parse(deadlineField.getText()), workerField.getText());
                titleField.clear();
                descriptionField.clear();
                priorityField.clear();
                deadlineField.clear();
                workerField.clear();
                updateTasksDisplay();
            } catch (Exception e) {
                showErrorAlert(e.getMessage());
            }
        });

        root.getChildren().addAll(newTaskLabel, titleField, descriptionField, priorityField, deadlineField, workerField, createTaskButton);

        // Удаление задачи
        Label deleteTaskLabel = new Label("Удалить задачу:");
        TextField deleteIndexField = new TextField();
        deleteIndexField.setPromptText("Индекс задачи");
        Button deleteTaskButton = new Button("Удалить задачу");
        deleteTaskButton.setOnAction(event -> {
            try {
                taskList.removeTask(Integer.parseInt(deleteIndexField.getText()));
                deleteIndexField.clear();
                updateTasksDisplay();
            } catch (Exception e) {
                showErrorAlert(e.getMessage());
            }
        });

        root.getChildren().addAll(deleteTaskLabel, deleteIndexField, deleteTaskButton);

        // Редактирование задачи
        Label editTaskLabel = new Label("Редактировать задачу:");
        TextField editIndexField = new TextField();
        editIndexField.setPromptText("Индекс задачи");
        TextField editTitleField = new TextField();
        editTitleField.setPromptText("Новый заголовок");
        TextField editDescriptionField = new TextField();
        editDescriptionField.setPromptText("Новое описание");
        TextField editPriorityField = new TextField();
        editPriorityField.setPromptText("Новый приоритет (1-4)");
        TextField editDeadlineField = new TextField();
        editDeadlineField.setPromptText("Новый дедлайн (ДД/ММ/ГГГГ)");
        TextField editWorkerField = new TextField();
        editWorkerField.setPromptText("Новый исполнитель");

        Button editTaskButton = new Button("Редактировать задачу");
        editTaskButton.setOnAction(event -> {
            try {
                taskList.editTask(Integer.parseInt(editIndexField.getText()), editTitleField.getText(), editDescriptionField.getText(),
                        Integer.parseInt(editPriorityField.getText()), editDeadlineField.getText().isEmpty() ? null : LocalDate.parse(editDeadlineField.getText()), editWorkerField.getText());
                editIndexField.clear();
                editTitleField.clear();
                editDescriptionField.clear();
                editPriorityField.clear();
                editDeadlineField.clear();
                editWorkerField.clear();
                updateTasksDisplay();
            } catch (Exception e) {
                showErrorAlert(e.getMessage());
            }
        });

        root.getChildren().addAll(editTaskLabel, editIndexField, editTitleField, editDescriptionField, editPriorityField, editDeadlineField, editWorkerField, editTaskButton);

        // Вывод всех задач
        Button printTasksButton = new Button("Вывести все задачи");
        ScrollPane scrollPane = new ScrollPane(tasksArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);

        printTasksButton.setOnAction(event -> updateTasksDisplay());

        root.getChildren().addAll(printTasksButton, scrollPane);

        // Отметить задачу выполненной
        Label doneTaskLabel = new Label("Отметить задачу выполненной:");
        TextField doneIndexField = new TextField();
        doneIndexField.setPromptText("Индекс задачи");
        Button doneTaskButton = new Button("Отметить выполненной");
        doneTaskButton.setOnAction(event -> {
            try {
                taskList.editDone(Integer.parseInt(doneIndexField.getText()));
                doneIndexField.clear();
                updateTasksDisplay();
            } catch (Exception e) {
                showErrorAlert(e.getMessage());
            }
        });

        root.getChildren().addAll(doneTaskLabel, doneIndexField, doneTaskButton);

        // Создание сцены и отображение окна
        Scene scene = new Scene(root, 600, 800);
        primaryStage.setTitle("Управление задачами");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void updateTasksDisplay() {
        tasksArea.setText(taskList.printAllTasks());
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}