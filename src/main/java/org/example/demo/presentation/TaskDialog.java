package org.example.demo.presentation;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.example.demo.domain.Task;
import org.example.demo.domain.exceptions.IncorrectTask;
import javafx.geometry.Insets;
import java.time.LocalDate;
import java.util.Optional;
import javafx.beans.value.ChangeListener;

public class TaskDialog extends Dialog<Task> {
    private final String projectName;
    private Task existingTask;

    public TaskDialog(String projectName) {
        this.projectName = projectName;
        setTitle("Создание новой задачи");
        setHeaderText("Добавление задачи в проект: " + projectName);

        // Элементы формы
        TextField titleField = new TextField();
        titleField.setPromptText("Название задачи");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Описание");
        descriptionArea.setPrefRowCount(3);

        ComboBox<Integer> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll(1, 2, 3, 4);
        priorityCombo.setValue(3);

        DatePicker deadlinePicker = new DatePicker();
        deadlinePicker.setValue(LocalDate.now().plusDays(7));

        TextField workerField = new TextField();
        workerField.setPromptText("Исполнитель");

        // Расположение элементов
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Название*:"), titleField);
        grid.addRow(1, new Label("Описание*:"), descriptionArea);
        grid.addRow(2, new Label("Приоритет:"), priorityCombo);
        grid.addRow(3, new Label("Дедлайн:"), deadlinePicker);
        grid.addRow(4, new Label("Исполнитель:"), workerField);

        getDialogPane().setContent(grid);

        // Кнопки
        ButtonType createButtonType = new ButtonType("Создать", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Валидация
        Button createButton = (Button) getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);

        titleField.textProperty().addListener((obs, old, newVal) -> validateForm(createButton, titleField, descriptionArea));
        descriptionArea.textProperty().addListener((obs, old, newVal) -> validateForm(createButton, titleField, descriptionArea));

        setResultConverter(buttonType -> {
            if (buttonType == createButtonType) {
                try {
                    Task task = new Task(
                            titleField.getText(),
                            descriptionArea.getText(),
                            priorityCombo.getValue(),
                            deadlinePicker.getValue(),
                            workerField.getText()
                    );
                    task.setProject(this.projectName); // Устанавливаем проект через сеттер
                    return task;
                } catch (IncorrectTask e) {
                    showErrorDialog(e.getMessage());
                    return null;
                }
            }
            return null;
        });
    }

    // Основной конструктор для редактирования существующей задачи
    public TaskDialog(String projectName, Task existingTask) {
        this.projectName = projectName;
        this.existingTask = existingTask;

        // Настройка заголовка
        setTitle(existingTask != null ? "Редактирование задачи" : "Создание новой задачи");
        setHeaderText((existingTask != null ? "Редактирование" : "Добавление") +
                " задачи в проект: " + projectName);

        // Элементы формы
        TextField titleField = new TextField();
        TextArea descriptionArea = new TextArea();
        ComboBox<Integer> priorityCombo = new ComboBox<>();
        DatePicker deadlinePicker = new DatePicker();
        TextField workerField = new TextField();

        // Инициализация полей
        priorityCombo.getItems().addAll(1, 2, 3, 4);
        descriptionArea.setPrefRowCount(3);

        // Заполнение значений для существующей задачи
        if (existingTask != null) {
            titleField.setText(existingTask.getTitle());
            descriptionArea.setText(existingTask.getDescription());
            priorityCombo.setValue(existingTask.getPriority());
            deadlinePicker.setValue(existingTask.getDeadline());
            workerField.setText(existingTask.getWorker());
        } else {
            priorityCombo.setValue(3);
            deadlinePicker.setValue(LocalDate.now().plusDays(7));
        }

        // Разметка формы
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        grid.addRow(0, new Label("Название*:"), titleField);
        grid.addRow(1, new Label("Описание*:"), descriptionArea);
        grid.addRow(2, new Label("Приоритет:"), priorityCombo);
        grid.addRow(3, new Label("Дедлайн:"), deadlinePicker);
        grid.addRow(4, new Label("Исполнитель:"), workerField);

        getDialogPane().setContent(grid);

        // Кнопки
        ButtonType confirmButtonType = new ButtonType(
                existingTask != null ? "Сохранить" : "Создать",
                ButtonBar.ButtonData.OK_DONE
        );
        getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        // Валидация
        Button confirmButton = (Button) getDialogPane().lookupButton(confirmButtonType);
        confirmButton.setDisable(true);

        ChangeListener<String> validationListener = (obs, old, newVal) -> {
            confirmButton.setDisable(
                    titleField.getText().trim().isEmpty() ||
                            descriptionArea.getText().trim().isEmpty()
            );
        };

        titleField.textProperty().addListener(validationListener);
        descriptionArea.textProperty().addListener(validationListener);

        // Обработка результата
        setResultConverter(buttonType -> {
            if (buttonType == confirmButtonType) {
                try {
                    String title = titleField.getText().trim();
                    String description = descriptionArea.getText().trim();
                    int priority = priorityCombo.getValue();
                    LocalDate deadline = deadlinePicker.getValue();
                    String worker = workerField.getText().trim();

                    if (existingTask != null) {
                        // Для существующей задачи - создаем новый объект с теми же параметрами
                        Task updatedTask = new Task(
                                title, description, priority, deadline, worker
                        );
                        updatedTask.setId(existingTask.getId());
                        updatedTask.setProject(projectName);
                        updatedTask.setDone(existingTask.isDone());
                        return updatedTask;
                    } else {
                        // Для новой задачи
                        Task newTask = new Task(
                                title, description, priority, deadline, worker
                        );
                        newTask.setProject(projectName);
                        return newTask;
                    }
                } catch (IncorrectTask e) {
                    showErrorDialog(e.getMessage());
                    return null;
                }
            }
            return null;
        });
    }


    private void validateForm(Button button, TextField title, TextArea description) {
        button.setDisable(
                title.getText().trim().isEmpty() ||
                        description.getText().trim().isEmpty()
        );
    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка ввода");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}