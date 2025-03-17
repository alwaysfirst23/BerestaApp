package org.example.demo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.time.format.DateTimeParseException;

public class TaskApp extends Application {
    private TaskList taskList = new TaskList();
    private TextArea tasksArea = new TextArea();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy"); // Добавляем форматтер

    // Объявляем поля как поля класса
    private TextField titleField;
    private TextField descriptionField;
    private TextField priorityField;
    private TextField deadlineField;
    private TextField workerField;

    private TextField editIndexField;
    private TextField editTitleField;
    private TextField editDescriptionField;
    private TextField editPriorityField;
    private TextField editDeadlineField;
    private TextField editWorkerField;

    private void clearFields() {
        titleField.clear();
        descriptionField.clear();
        priorityField.clear();
        deadlineField.clear();
        workerField.clear();
    }

    private void clearEditFields() {
        editIndexField.clear();
        editTitleField.clear();
        editDescriptionField.clear();
        editPriorityField.clear();
        editDeadlineField.clear();
        editWorkerField.clear();
    }

    /*
        Метод start - основной, с него начинается выполнение программы
     */

    @Override
    public void start(Stage primaryStage) {

        // Создание контейнера, в который будем помещать элементы
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);



        /*
            Шаблон сооздания новой задачи

            Создаётся текст и поля для ввода текста
            Создаётся кнопка
            root.getChildren().addAll добавляет все созданные
            элементы в контейнер, располагая их по центру
            экрана вертикально друг под другом
        */

        Label newTaskLabel = new Label("Создать новую задачу:"); // Так создаётся текст

        titleField = new TextField(); // Так создаются поля для ввода текста
        titleField.setPromptText("Заголовок");
        descriptionField = new TextField();
        descriptionField.setPromptText("Описание");
        priorityField = new TextField();
        priorityField.setPromptText("Приоритет (1-4)");
        deadlineField = new TextField();
        deadlineField.setPromptText("Дедлайн (ДД/ММ/ГГГГ)");
        workerField = new TextField();
        workerField.setPromptText("Исполнитель");

        Button createTaskButton = getCreateButton(); // Так создаётся кнопка

        root.getChildren().addAll(newTaskLabel, titleField, descriptionField, priorityField, deadlineField, workerField, createTaskButton);



        /*
            Эта штука работает в точности так же, как и создание задачи
            Дополнительно просит индекс задачи
            Обновляет параметры задачи по индексу
         */

        Label editTaskLabel = new Label("Редактировать задачу:");

        editIndexField = new TextField();
        editIndexField.setPromptText("Индекс задачи");
        editTitleField = new TextField();
        editTitleField.setPromptText("Новый заголовок");
        editDescriptionField = new TextField();
        editDescriptionField.setPromptText("Новое описание");
        editPriorityField = new TextField();
        editPriorityField.setPromptText("Новый приоритет (1-4)");
        editDeadlineField = new TextField();
        editDeadlineField.setPromptText("Новый дедлайн (ДД/ММ/ГГГГ)");
        editWorkerField = new TextField();
        editWorkerField.setPromptText("Новый исполнитель");

        Button editTaskButton = getEditTaskButton();

        root.getChildren().addAll(editTaskLabel, editIndexField, editTitleField, editDescriptionField, editPriorityField, editDeadlineField, editWorkerField, editTaskButton);



        /*
            Здесь реализовано удаление задачи

            Всё так же создаём поле для индекса,
            создаём кнопку, которая при нажатии
            удаляет из списка нужную задачу
         */

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



        /*
            Так выводится список всех задач

            Здесь уже создаётся поле для вывода
            Оно сделано большим, но уменьшается под размер окна

            Также мы предполагаем, что все задачи туда могут не
            поместиться, поэтому создаём скролл-панель
         */

        Button printTasksButton = new Button("Вывести все задачи");

        tasksArea.setPrefHeight(300);  //  <--  Добавляем высоту для TextArea
        tasksArea.setWrapText(true);    //  Добавляем перенос строк
        ScrollPane scrollPane = new ScrollPane(tasksArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);

        printTasksButton.setOnAction(event -> updateTasksDisplay());

        root.getChildren().addAll(printTasksButton, scrollPane);



        /*
            Это нужно чтобы отметить задачу выполненной
            Просто изменяет статус на "Выполнено"
         */

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
        primaryStage.setTitle("Beresta");
        primaryStage.setScene(scene);
        primaryStage.show();
    }



    /*
        Этот метод делает кнопку "Создать задачу"

        При нажатии на кнопку, он считывает все данные, введённые
        пользователем, приводит их к нужным типам и создаёт задачу
        с помощью метода createTask

        После этого все поля очищаются, текст обновляется

        При вводе неправильных данных возвращает ошибку
     */

    @NotNull // Модификатор NotNull гарантирует, что метод вернёт хотя бы что-то, кнопка будет создана в любом случае
    private Button getCreateButton() {
        Button createTaskButton = new Button("Создать задачу");
        createTaskButton.setOnAction(event -> { // Здесь описывается, что будет делать кнопка при нажатии
            try {
                // Позволяет создать задачу, не указывая дедлайн
                LocalDate deadline = null;
                if (!deadlineField.getText().isEmpty()) {
                    deadline = LocalDate.parse(deadlineField.getText(), dateFormatter); // Используем форматтер
                }

                taskList.createTask(
                        titleField.getText(),
                        descriptionField.getText(),
                        Integer.parseInt(priorityField.getText()),
                        deadline,
                        workerField.getText()
                );

                clearFields();
                updateTasksDisplay();

            } catch (DateTimeParseException e) {
                showErrorAlert("Ошибка формата даты! Используйте ДД/ММ/ГГГГ");
            } catch (NumberFormatException e) {
                showErrorAlert("Приоритет должен быть числом от 1 до 4");
            } catch (Exception e) {
                showErrorAlert(e.getMessage());
            }
        });
        return createTaskButton;
    }



    /*
        Метод создаёт кнопку "Редактировать задачу"
        Работает в точности так же, как и getCreateButton
     */

    @NotNull
    private Button getEditTaskButton() {
        Button editTaskButton = new Button("Редактировать задачу");
        editTaskButton.setOnAction(event -> {
            try {
                LocalDate newDeadline = null;
                if (!editDeadlineField.getText().isEmpty()) {
                    newDeadline = LocalDate.parse(editDeadlineField.getText(), dateFormatter); // Используем форматтер
                }
                String priority_string = editPriorityField.getText();
                int priority_int = 0;
                if (priority_string != null && !(priority_string.trim().isEmpty())) priority_int = Integer.parseInt(priority_string);

                taskList.editTask(
                        Integer.parseInt(editIndexField.getText()),
                        editTitleField.getText(),
                        editDescriptionField.getText(),
                        priority_int,
                        newDeadline,
                        editWorkerField.getText()
                );

            } catch (DateTimeParseException e) {
                showErrorAlert("Ошибка формата даты! Используйте ДД/ММ/ГГГГ");
            } catch (NumberFormatException e) {
                showErrorAlert("Приоритет должен быть числом от 1 до 4");
            } catch (Exception e) {
                showErrorAlert(e.getMessage());
            }
            clearEditFields();
            updateTasksDisplay();
        });
        return editTaskButton;
    }

    // Обновляет список задач после каждого изменения
    private void updateTasksDisplay() {
        tasksArea.setText(taskList.printAllTasks());
    }

    // Выводит сообщение об ошибке на экран
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Точка входа в программу
    public static void main(String[] args) {
        launch(args);
    }
}