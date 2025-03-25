package org.example.demo;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.Comparator;
import javafx.geometry.Pos;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.time.format.DateTimeParseException;



public class TaskApp extends Application {

    private ObservableList<Task> tasks = FXCollections.observableArrayList();
    private ObservableList<Task> tasks2 = FXCollections.observableArrayList();
    private ObservableList<String> windowTitles = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        HBox topPanel = createTopPanel();
        root.setTop(topPanel);

        TabPane tabPane = createTabPane();
        root.setCenter(tabPane);

        // Кнопка создания окон только для первой вкладки
        StackPane bottomRightButton = createRoundButton();
        root.setBottom(bottomRightButton);
        BorderPane.setAlignment(bottomRightButton, Pos.BOTTOM_RIGHT);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Task Manager");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createTopPanel() {
        HBox hbox = new HBox(10);
        hbox.setPadding(new Insets(10));
        hbox.setStyle("-fx-background-color: #f0f0f0;");

        Button menuButton = new Button("☰");
        menuButton.setPrefSize(40, 40);
        setupMenuButton(menuButton);

        TextField searchField = new TextField();
        searchField.setPromptText("Поиск...");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        hbox.getChildren().addAll(menuButton, searchField);
        return hbox;
    }

    private void setupMenuButton(Button menuButton) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem themeItem = new MenuItem("Тема");
        MenuItem timeItem = new MenuItem("Тайм менеджмент");

        themeItem.setOnAction(e -> changeTheme());
        timeItem.setOnAction(e -> showTimeManagement());

        contextMenu.getItems().addAll(themeItem, timeItem);

        menuButton.setOnMouseEntered(e ->
                contextMenu.show(menuButton, e.getScreenX(), e.getScreenY())
        );

        menuButton.setOnMouseExited(e ->
                contextMenu.hide()
        );
    }

    private void changeTheme() {
        // Логика смены темы
        System.out.println("Смена темы...");
    }

    private void showTimeManagement() {
        // Логика тайм-менеджмента
        System.out.println("Открытие тайм-менеджмента...");
    }

    private StackPane createRoundButton() {
        Button addWindowButton = new Button("+");
        addWindowButton.getStyleClass().add("round-button");
        addWindowButton.setOnAction(e -> showCreateWindowDialog());

        StackPane stackPane = new StackPane(addWindowButton);
        stackPane.setPadding(new Insets(10));
        return stackPane;
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();

        Tab tab1 = new Tab("Проекты");
        tab1.setContent(createTaskContainer());
        tab1.setClosable(false); // Запрет закрытия вкладки

        Tab tab2 = new Tab("Мои задачи");
        tab2.setContent(createSortableTaskList());
        tab2.setClosable(false);

        Tab tab3 = new Tab("Статистика");
        tab3.setContent(new Label("Содержимое третьей вкладки"));
        tab3.setClosable(false);

        tabPane.getTabs().addAll(tab1, tab2, tab3);
        tabPane.getSelectionModel().selectFirst();

        return tabPane;
    }


    private ScrollPane createTaskContainer() {
        HBox tasksHBox = new HBox(15);
        tasksHBox.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(tasksHBox);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setFitToHeight(true);

        // Динамическое обновление содержимого
        windowTitles.addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends String> change) {
                while (change.next()) {
                    if (change.wasAdded()) {
                        tasksHBox.getChildren().clear();
                        for (String title : windowTitles) {
                            VBox taskWindow = createTaskWindow(title, tasks);
                            tasksHBox.getChildren().add(taskWindow);
                        }
                    }
                }
            }
        });

        return scrollPane;
    }

    private VBox createTaskWindow(String title, ObservableList<Task> taskList) {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.setStyle("-fx-background-color: white; -fx-border-color: #ddd;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold;");

        ListView<Task> taskListView = new ListView<>(taskList);
        taskListView.setPrefHeight(400);
        taskListView.setCellFactory(lv -> new ListCell<Task>() {
            @Override
            protected void updateItem(Task item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    String status = item.isDone() ? "✅ Выполнено" : "❌ Не выполнено";
                    String priority = item.whichPriority(item.getPriority());
                    setText(String.format(
                            "%s (%s) - %s\nПриоритет: %s",
                            item.getTitle(),
                            item.getFormattedDeadline("dd.MM.yyyy"),
                            status,
                            priority
                    ));
                }
            }
        });

        // Добавление двойного клика для редактирования
        taskListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Task selected = taskListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showEditTaskDialog(selected, taskList);
                }
            }
        });

        ScrollPane listScroll = new ScrollPane(taskListView);
        listScroll.setFitToWidth(true);

        Button addButton = new Button("+");
        addButton.setPrefSize(40, 40);
        addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addButton.setOnAction(e -> showTaskDialog(taskList));

        VBox controls = new VBox(5);
        controls.setAlignment(Pos.CENTER);
        controls.getChildren().add(addButton);

        vbox.getChildren().addAll(titleLabel, listScroll, controls);
        return vbox;
    }

    private void showEditTaskDialog(Task task, ObservableList<Task> taskList) {
        Dialog<Task> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Редактирование задачи");
        dialog.setHeaderText("Измените данные задачи");

        TextField titleField = new TextField(task.getTitle());
        titleField.setPromptText("Заголовок");
        TextField descriptionField = new TextField(task.getDescription());
        descriptionField.setPromptText("Описание");
        ChoiceBox<String> priorityBox = new ChoiceBox<>();
        priorityBox.getItems().addAll(
                "1 - Вообще не срочно",
                "2 - Не особо срочно",
                "3 - Срочно",
                "4 - Очень срочно!"
        );
        priorityBox.getSelectionModel().select(task.getPriority() - 1);
        DatePicker deadlinePicker = new DatePicker(task.getDeadline());
        TextField workerField = new TextField(task.getWorker());
        workerField.setPromptText("Исполнитель");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.add(new Label("Заголовок:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Описание:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Приоритет:"), 0, 2);
        grid.add(priorityBox, 1, 2);
        grid.add(new Label("Дедлайн:"), 0, 3);
        grid.add(deadlinePicker, 1, 3);
        grid.add(new Label("Исполнитель:"), 0, 4);
        grid.add(workerField, 1, 4);

        dialog.getDialogPane().setContent(grid);
        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    // Проверка на null
                    if (titleField.getText().trim().isEmpty() ||
                            descriptionField.getText().trim().isEmpty()) {
                        throw new IllegalArgumentException("Заголовок и описание не могут быть пустыми");
                    }

                    // Валидация дедлайна
                    LocalDate deadline = deadlinePicker.getValue();
                    if (deadline == null) {
                        throw new IllegalArgumentException("Укажите дедлайн");
                    }
                    if (deadline.isBefore(LocalDate.now())) {
                        throw new IllegalArgumentException("Дедлайн не может быть в прошлом");
                    }

                    int priority = Integer.parseInt(priorityBox.getValue().split(" - ")[0]);
                    task.setTitle(titleField.getText());
                    task.setDescription(descriptionField.getText());
                    task.setPriority(priority);
                    task.setDeadline(deadline);
                    task.setWorker(workerField.getText());
                    return task;
                } catch (Exception ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText("Некорректные данные");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait();
    }


    private void showTaskDialog(ObservableList<Task> taskList) {
        Dialog<Task> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Новая задача");
        dialog.setHeaderText("Введите данные задачи");

        TextField titleField = new TextField();
        titleField.setPromptText("Заголовок");
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Описание");
        ChoiceBox<String> priorityBox = new ChoiceBox<>();
        priorityBox.getItems().addAll(
                "1 - Вообще не срочно",
                "2 - Не особо срочно",
                "3 - Срочно",
                "4 - Очень срочно!"
        );
        priorityBox.getSelectionModel().select(0);
        DatePicker deadlinePicker = new DatePicker();
        TextField workerField = new TextField();
        workerField.setPromptText("Исполнитель");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.add(new Label("Заголовок:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Описание:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Приоритет:"), 0, 2);
        grid.add(priorityBox, 1, 2);
        grid.add(new Label("Дедлайн:"), 0, 3);
        grid.add(deadlinePicker, 1, 3);
        grid.add(new Label("Исполнитель:"), 0, 4);
        grid.add(workerField, 1, 4);

        dialog.getDialogPane().setContent(grid);
        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    // Проверка на null и пустые поля
                    if (titleField.getText().trim().isEmpty() ||
                            descriptionField.getText().trim().isEmpty()) {
                        throw new IllegalArgumentException("Заголовок и описание не могут быть пустыми");
                    }

                    // Валидация дедлайна
                    LocalDate deadline = deadlinePicker.getValue();
                    if (deadline == null) {
                        throw new IllegalArgumentException("Укажите дедлайн");
                    }
                    if (deadline.isBefore(LocalDate.now())) {
                        throw new IllegalArgumentException("Дедлайн не может быть в прошлом");
                    }

                    // Валидация приоритета
                    if (priorityBox.getValue() == null) {
                        throw new IllegalArgumentException("Выберите приоритет");
                    }

                    int priority = Integer.parseInt(priorityBox.getValue().split(" - ")[0]);
                    Task task = new Task(
                            titleField.getText(),
                            descriptionField.getText(),
                            priority,
                            deadline,
                            workerField.getText()
                    );
                    taskList.add(task);
                    return task;
                } catch (Exception ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText("Некорректные данные");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait();
    }


    private VBox createSortableTaskList() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(15));

        HBox controls = new HBox(10);
        ComboBox<String> sortCombo = new ComboBox<>(
                FXCollections.observableArrayList(
                        "По дедлайну",
                        "По приоритету"
                )
        );
        sortCombo.setValue("По дедлайну");

        Button sortBtn = new Button("Сортировать");
        controls.getChildren().addAll(
                new Label("Сортировка:"), sortCombo, sortBtn
        );

        ListView<Task> listView = new ListView<>();
        listView.setCellFactory(lv -> new ListCell<Task>() {
            @Override
            protected void updateItem(Task item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    String status = item.isDone() ? "✅ Выполнено" : "❌ Не выполнено";
                    String priority = item.whichPriority(item.getPriority());
                    setText(String.format(
                            "%s (%s) - %s\nПриоритет: %s",
                            item.getTitle(),
                            item.getFormattedDeadline("dd.MM.yyyy"),
                            status,
                            priority
                    ));
                }
            }
        });

        SortedList<Task> sortedTasks = new SortedList<>(tasks2);
        listView.setItems(sortedTasks);

        sortBtn.setOnAction(e -> {
            String sortBy = sortCombo.getValue();
            sortedTasks.setComparator(getComparator(sortBy));
        });

        container.getChildren().addAll(controls, listView);
        return container;
    }

    private void showCreateWindowDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Новое окно");
        dialog.setHeaderText("Введите название окна");

        TextField titleField = new TextField();
        titleField.setPromptText("Название окна");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.add(new Label("Название:"), 0, 0);
        grid.add(titleField, 1, 0);

        dialog.getDialogPane().setContent(grid);
        ButtonType saveButtonType = new ButtonType("Создать", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String title = titleField.getText().trim();
                if (!title.isEmpty()) {
                    windowTitles.add(title);
                    return title;
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private Comparator<Task> getComparator(String sortBy) {
        return switch (sortBy) {
            case "По дедлайну" -> Comparator.comparing(Task::getDeadline);
            case "По приоритету" -> Comparator.comparingInt(Task::getPriority).reversed();
            default -> Comparator.comparing(Task::getTitle);
        };
    }

    public static void main(String[] args) {
        launch(args);
    }
}
