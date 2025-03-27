package org.example.demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.util.Comparator;
import javafx.geometry.Pos;


public class TaskApp extends Application {
    private ObservableList<Task> allTasks = FXCollections.observableArrayList(); // Все задачи
    private ObservableList<Task> tasksForMyTasksTab = FXCollections.observableArrayList(); // Для вкладки "Мои задачи"
    private ObservableList<String> projectTitles = FXCollections.observableArrayList(); // Названия проектов
    private TaskBase taskBase = new TaskBase();

    @Override
    public void start(Stage primaryStage) {
        // Загружаем задачи из базы данных при старте
        loadTasksFromDatabase();
        // Добавляем хотя бы один проект по умолчанию, если их нет
        if (projectTitles.isEmpty()) {
            projectTitles.add("Основной проект");
        }

        BorderPane root = new BorderPane();

        HBox topPanel = createTopPanel();
        root.setTop(topPanel);

        TabPane tabPane = createTabPane();
        root.setCenter(tabPane);

        StackPane bottomRightButton = createRoundButton();
        root.setBottom(bottomRightButton);
        BorderPane.setAlignment(bottomRightButton, Pos.BOTTOM_RIGHT);

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setTitle("Task Manager");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadTasksFromDatabase() {
        try {
            String sql = "SELECT * FROM tasks";
            try (Connection conn = DatabaseConnector.taskConnect();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                allTasks.clear();
                tasksForMyTasksTab.clear();

                while (rs.next()) {
                    Task task = new Task(
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getInt("priority"),
                            rs.getString("deadline") != null ?
                                    LocalDate.parse(rs.getString("deadline")) : null,
                            rs.getString("worker")
                    );
                    task.setId(rs.getInt("id"));
                    task.setDone(rs.getInt("is_done") == 1);
                    task.setProject(rs.getString("project"));

                    allTasks.add(task);
                    tasksForMyTasksTab.add(task);

                    // Добавляем проект в список, если его там нет
                    if (task.getProject() != null && !task.getProject().isEmpty()
                            && !projectTitles.contains(task.getProject())) {
                        projectTitles.add(task.getProject());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка загрузки задач: " + e.getMessage());
        }
    }

    private HBox createTopPanel() {
        HBox hbox = new HBox(10);
        hbox.setPadding(new Insets(10));
        hbox.setStyle("-fx-background-color: #f0f0f0;");

        Button profileButton = new Button("Мой профиль");
        //profileButton.setPrefSize(100, 40);
        profileButton.getStyleClass().add("profile-button");
        profileButton.setOnAction(e -> showProfile());

        TextField searchField = new TextField();
        searchField.setPromptText("Поиск...");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button menuButton = new Button("☰");
        menuButton.setPrefSize(30, 30);
        setupMenuButton(menuButton);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        hbox.getChildren().addAll(profileButton, spacer, searchField, menuButton);
        return hbox;
    }

    private void setupMenuButton(Button menuButton) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem themeItem = new MenuItem("Тема");
        MenuItem timeItem = new MenuItem("Тайм менеджмент");

        themeItem.setOnAction(e -> {
            changeTheme();
            contextMenu.hide(); // Закрыть меню после выбора темы
        });

        timeItem.setOnAction(e -> {
            showTimeManagement();
            contextMenu.hide(); // Закрыть меню после выбора тайм-менеджмента
        });

        contextMenu.getItems().addAll(themeItem, timeItem);
        // Показывать меню при клике
        menuButton.setOnMouseClicked(e -> {
            contextMenu.show(menuButton, e.getScreenX(), e.getScreenY());
        });
    }


    private void changeTheme() {
        // Логика смены темы
        System.out.println("Тема...");
    }

    private void showTimeManagement() {
        // Логика тайм-менеджмента
        System.out.println("Тайм-менеджмент...");
    }

    private void showProfile() {
        // Логика просмотра профиля
        System.out.println("Профиль...");
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

        // Динамическое обновление при добавлении проектов
        projectTitles.addListener((ListChangeListener<String>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (String title : change.getAddedSubList()) {
                        VBox taskWindow = createTaskWindow(title);
                        tasksHBox.getChildren().add(taskWindow);
                    }
                }
            }
        });

        // Инициализация существующих проектов
        for (String title : projectTitles) {
            VBox taskWindow = createTaskWindow(title);
            tasksHBox.getChildren().add(taskWindow);
        }

        return scrollPane;
    }

    private VBox createTaskWindow(String projectTitle) {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.setStyle("-fx-background-color: white; -fx-border-color: #ddd;");

        Label titleLabel = new Label(projectTitle);
        titleLabel.setStyle("-fx-font-weight: bold;");

        // Фильтруем задачи по текущему проекту
        ObservableList<Task> projectTasks = allTasks.filtered(task ->
                projectTitle.equals(task.getProject())
        );

        ListView<Task> taskListView = new ListView<>(projectTasks);
        taskListView.setPrefHeight(400);
        taskListView.setCellFactory(lv -> new ListCell<Task>() {
            @Override
            protected void updateItem(Task item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Основной контейнер для содержимого задачи
                    VBox contentBox = new VBox(5);
                    contentBox.setPadding(new Insets(5));

                    // Заголовок задачи
                    Label titleLabel = new Label(item.getTitle());
                    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
                    titleLabel.setWrapText(true);
                    titleLabel.setOnMouseClicked(e -> showEditTaskDialog(item, projectTasks));

                    // Описание задачи (теперь всегда видимо)
                    Label descriptionLabel = new Label(item.getDescription());
                    descriptionLabel.setWrapText(true);
                    descriptionLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 12;");

                    // Информация о задаче
                    HBox infoBox = new HBox(10);
                    infoBox.setAlignment(Pos.CENTER_LEFT);

                    Label workerLabel = new Label("👤 " + item.getWorker());
                    workerLabel.setStyle("-fx-font-size: 12;");

                    Label deadlineLabel = new Label("📅 " + item.getFormattedDeadline("dd.MM.yyyy"));
                    deadlineLabel.setStyle("-fx-font-size: 12;");

                    Label priorityLabel = new Label("⚡ " + item.whichPriority(item.getPriority()));
                    priorityLabel.setStyle("-fx-font-size: 12;");

                    infoBox.getChildren().addAll(workerLabel, deadlineLabel, priorityLabel);

                    // Кнопка изменения статуса
                    Button markDoneButton = new Button(item.isDone() ? "✅ Выполнено" : "Отметить как выполненное");
                    markDoneButton.setStyle("-fx-font-size: 12;");
                    markDoneButton.setOnAction(e -> {
                        boolean newStatus = !item.isDone();
                        try {
                            taskBase.updateTaskStatus(getTaskId(item), newStatus);
                            item.setDone(newStatus);
                            markDoneButton.setText(newStatus ? "✅ Выполнено" : "Отметить как выполненное");
                            setStyle(newStatus ? "-fx-text-fill: gray;" : "-fx-text-fill: black;");
                            taskListView.refresh();
                        } catch (SQLException ex) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Ошибка");
                            alert.setHeaderText("Не удалось обновить статус");
                            alert.setContentText(ex.getMessage());
                            alert.showAndWait();
                        }
                    });

                    // Собираем все элементы вместе
                    contentBox.getChildren().addAll(
                            titleLabel,
                            descriptionLabel,
                            infoBox,
                            markDoneButton
                    );

                    setGraphic(contentBox);
                    setStyle(item.isDone() ? "-fx-text-fill: gray;" : "-fx-text-fill: black;");
                }
            }
        });

        ScrollPane listScroll = new ScrollPane(taskListView);
        listScroll.setFitToWidth(true);

        // Кнопка добавления новой задачи
        Button addButton = new Button("+");
        addButton.getStyleClass().add("round-button-small");
        addButton.setOnAction(e -> {
            Dialog<Task> dialog = createTaskDialog(projectTitle);
            dialog.showAndWait();
        });

        VBox controls = new VBox(5);
        controls.setAlignment(Pos.CENTER);
        controls.getChildren().add(addButton);

        vbox.getChildren().addAll(titleLabel, listScroll, controls);
        return vbox;
    }

    private Dialog<Task> createTaskDialog(String projectTitle) {
        Dialog<Task> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Новая задача");
        dialog.setHeaderText("Введите данные задачи");

        // Создаем элементы формы
        TextField titleField = new TextField();
        titleField.setPromptText("Заголовок");
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Описание");

        ChoiceBox<String> priorityBox = new ChoiceBox<>();
        priorityBox.getItems().addAll(
                "1 - Не срочно",
                "2 - Средне",
                "3 - Срочно",
                "4 - Очень срочно!"
        );
        priorityBox.getSelectionModel().select(0);

        DatePicker deadlinePicker = new DatePicker();
        TextField workerField = new TextField();
        workerField.setPromptText("Исполнитель");

        ComboBox<String> projectCombo = new ComboBox<>(projectTitles);
        projectCombo.setPromptText("Выберите проект");
        projectCombo.getSelectionModel().select(projectTitle);

        // Создаем layout для формы
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

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
        grid.add(new Label("Проект:"), 0, 5);
        grid.add(projectCombo, 1, 5);

        // Устанавливаем содержимое диалога
        dialog.getDialogPane().setContent(grid);

        // Добавляем кнопки
        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Обработчик результата
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    // Валидация полей
                    if (titleField.getText().trim().isEmpty()) {
                        throw new IllegalArgumentException("Заголовок не может быть пустым");
                    }
                    if (descriptionField.getText().trim().isEmpty()) {
                        throw new IllegalArgumentException("Описание не может быть пустым");
                    }

                    LocalDate deadline = deadlinePicker.getValue();
                    if (deadline == null) {
                        throw new IllegalArgumentException("Укажите дедлайн");
                    }
                    if (deadline.isBefore(LocalDate.now())) {
                        throw new IllegalArgumentException("Дедлайн не может быть в прошлом");
                    }

                    String selectedProject = projectCombo.getValue();
                    if (selectedProject == null || selectedProject.trim().isEmpty()) {
                        throw new IllegalArgumentException("Необходимо выбрать проект");
                    }

                    // Получаем приоритет
                    int priority = Integer.parseInt(priorityBox.getValue().split(" - ")[0]);

                    // Создаем задачу в БД
                    taskBase.createTask(
                            titleField.getText(),
                            descriptionField.getText(),
                            priority,
                            deadline,
                            workerField.getText(),
                            selectedProject
                    );

                    // Обновляем данные
                    loadTasksFromDatabase();

                    // Возвращаем новую задачу
                    Task newTask = new Task(
                            titleField.getText(),
                            descriptionField.getText(),
                            priority,
                            deadline,
                            workerField.getText()
                    );
                    newTask.setProject(selectedProject);
                    return newTask;

                } catch (Exception ex) {
                    // Показываем ошибку пользователю
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

        return dialog;
    }

    private void showEditTaskDialog(Task task, ObservableList<Task> observableList) {
        Dialog<Task> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Редактирование задачи");
        dialog.setHeaderText("Измените данные задачи");

        // Создаем элементы формы
        TextField titleField = new TextField(task.getTitle());
        titleField.setPromptText("Заголовок");

        TextField descriptionField = new TextField(task.getDescription());
        descriptionField.setPromptText("Описание");

        ChoiceBox<String> priorityBox = new ChoiceBox<>(FXCollections.observableArrayList(
                "1 - Вообще не срочно",
                "2 - Не особо срочно",
                "3 - Срочно",
                "4 - Очень срочно!"
        ));
        priorityBox.getSelectionModel().select(task.getPriority() - 1);

        DatePicker deadlinePicker = new DatePicker(task.getDeadline());
        TextField workerField = new TextField(task.getWorker());

        ComboBox<String> projectCombo = new ComboBox<>(projectTitles);
        projectCombo.setValue(task.getProject());

        // Размещаем элементы в GridPane
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

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
        grid.add(new Label("Проект:"), 0, 5);
        grid.add(projectCombo, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Обработка результата
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    // Валидация данных
                    if (titleField.getText().trim().isEmpty() || descriptionField.getText().trim().isEmpty()) {
                        throw new IllegalArgumentException("Все поля должны быть заполнены");
                    }

                    LocalDate deadline = deadlinePicker.getValue();
                    if (deadline == null) {
                        throw new IllegalArgumentException("Укажите дедлайн");
                    }

                    int priority = Integer.parseInt(priorityBox.getValue().split(" - ")[0]);
                    String project = projectCombo.getValue();

                    // Обновляем задачу в базе данных
                    boolean updated = taskBase.editTask(
                            task.getId(),  // Используем ID из объекта
                            titleField.getText(),
                            descriptionField.getText(),
                            priority,
                            deadline,
                            workerField.getText(),
                            project,
                            task.isDone()
                    );

                    if (!updated) {
                        throw new RuntimeException("Не удалось обновить задачу в БД");
                    }

                    // Обновляем UI
//                    Platform.runLater(() -> {
//                        task.setTitle(titleField.getText());
//                        task.setDescription(descriptionField.getText());
//                        task.setPriority(priority);
//                        task.setDeadline(deadline);
//                        task.setWorker(workerField.getText());
//                        task.setProject(project);
//
//                        // Обновляем ListView
//                        observableList.setAll(allTasks);
//                    });
                    // Находим задачу в allTasks по ID (предполагая, что Task.equals() сравнивает ID)
                    Task taskToUpdate = allTasks.stream()
                            .filter(t -> t.getId() == task.getId())
                            .findFirst()
                            .orElse(null);

                    if (taskToUpdate != null) {
                        // Обновляем поля
                        taskToUpdate.setTitle(titleField.getText());
                        taskToUpdate.setDescription(descriptionField.getText());
                        taskToUpdate.setPriority(priority);
                        taskToUpdate.setDeadline(deadline);
                        taskToUpdate.setWorker(workerField.getText());
                        taskToUpdate.setProject(project);
                    }

                    // Обновляем tasksForMyTasksTab (если нужно)
                    Task taskInMyTasks = tasksForMyTasksTab.stream()
                            .filter(t -> t.getId() == task.getId())
                            .findFirst()
                            .orElse(null);

                    if (taskInMyTasks != null) {
                        taskInMyTasks.setTitle(titleField.getText());
                        taskInMyTasks.setDescription(descriptionField.getText());
                        taskInMyTasks.setPriority(priority);
                        taskInMyTasks.setDeadline(deadline);
                        taskInMyTasks.setWorker(workerField.getText());
                        taskInMyTasks.setProject(project);
                    }

                    return task;
                } catch (Exception ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText("Ошибка при редактировании");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private int getTaskId(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        return task.getId();  // Просто берем ID из объекта
    }


    private void showTaskDialog(ObservableList<Task> observableList) {
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
                "1 - Не срочно",
                "2 - Средне",
                "3 - Срочно",
                "4 - Очень срочно!"
        );
        priorityBox.getSelectionModel().select(0);
        DatePicker deadlinePicker = new DatePicker();
        TextField workerField = new TextField();
        workerField.setPromptText("Исполнитель");
        ComboBox<String> projectCombo = new ComboBox<>(projectTitles);
        projectCombo.setPromptText("Выберите проект");


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
        grid.add(new Label("Проект:"), 0, 5);
        grid.add(projectCombo, 1, 5);

        dialog.getDialogPane().setContent(grid);
        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    if (titleField.getText().trim().isEmpty() ||
                            descriptionField.getText().trim().isEmpty()) {
                        throw new IllegalArgumentException("Заголовок и описание не могут быть пустыми");
                    }

                    LocalDate deadline = deadlinePicker.getValue();
                    if (deadline == null) {
                        throw new IllegalArgumentException("Укажите дедлайн");
                    }
                    if (deadline.isBefore(LocalDate.now())) {
                        throw new IllegalArgumentException("Дедлайн не может быть в прошлом");
                    }

                    int priority = Integer.parseInt(priorityBox.getValue().split(" - ")[0]);

                    // Создаем задачу в базе данных
                    taskBase.createTask(
                            titleField.getText(),
                            descriptionField.getText(),
                            priority,
                            deadline,
                            workerField.getText(),
                            projectCombo.getValue() // Добавляем проект
                    );

                    // Перезагружаем задачи из базы
                    loadTasksFromDatabase();

                    return new Task(
                            titleField.getText(),
                            descriptionField.getText(),
                            priority,
                            deadline,
                            workerField.getText()
                    );
                } catch (Exception ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText("Некорректные данные");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
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
                        "По приоритету",
                        "По проекту",  // Добавлена новая опция сортировки
                        "По статусу"   // Добавлена новая опция сортировки
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
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    // Создаем VBox для лучшего форматирования
                    VBox contentBox = new VBox(5);

                    // Статус задачи
                    Label statusLabel = new Label(item.isDone() ? "✅ Выполнено" : "❌ Не выполнено");
                    statusLabel.setStyle("-fx-font-weight: bold;");

                    // Название задачи
                    Label titleLabel = new Label(item.getTitle());
                    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

                    // Дедлайн
                    Label deadlineLabel = new Label("📅 " + item.getFormattedDeadline("dd.MM.yyyy"));

                    // Приоритет
                    Label priorityLabel = new Label("⚡ " + item.whichPriority(item.getPriority()));

                    // Проект (если есть)
                    if (item.getProject() != null && !item.getProject().isEmpty()) {
                        Label projectLabel = new Label("📁 " + item.getProject());
                        contentBox.getChildren().add(projectLabel);
                    }

                    // Исполнитель
                    Label workerLabel = new Label("👤 " + item.getWorker());

                    // Добавляем все элементы
                    contentBox.getChildren().addAll(
                            statusLabel,
                            titleLabel,
                            deadlineLabel,
                            priorityLabel,
                            workerLabel
                    );

                    setGraphic(contentBox);
                    setStyle(item.isDone() ? "-fx-text-fill: gray;" : "-fx-text-fill: black;");
                }
            }
        });

        SortedList<Task> sortedTasks = new SortedList<>(tasksForMyTasksTab);  // Используем обновленное имя списка
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
        dialog.setTitle("Новый проект");
        dialog.setHeaderText("Введите название нового проекта");

        TextField titleField = new TextField();
        titleField.setPromptText("Название проекта");

        // Добавляем валидацию
        titleField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                titleField.setStyle("-fx-border-color: red;");
            } else {
                titleField.setStyle("");
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.add(new Label("Название проекта:"), 0, 0);
        grid.add(titleField, 1, 0);

        dialog.getDialogPane().setContent(grid);
        ButtonType saveButtonType = new ButtonType("Создать", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String title = titleField.getText().trim();
                if (!title.isEmpty()) {
                    // Проверяем, нет ли уже проекта с таким названием
                    if (projectTitles.contains(title)) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Предупреждение");
                        alert.setHeaderText("Проект уже существует");
                        alert.setContentText("Проект с таким названием уже существует.");
                        alert.showAndWait();
                        return null;
                    }
                    projectTitles.add(title);
                    return title;
                }
            }
            return null;
        });

        // Разрешаем кнопку "Создать" только при введенном тексте
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);
        titleField.textProperty().addListener((obs, oldVal, newVal) -> {
            saveButton.setDisable(newVal.trim().isEmpty());
        });

        dialog.showAndWait();
    }

    private Comparator<Task> getComparator(String sortBy) {
        return switch (sortBy) {
            case "По дедлайну" -> Comparator.comparing(Task::getDeadline);
            case "По приоритету" -> Comparator.comparingInt(Task::getPriority).reversed();
            case "По проекту" -> Comparator.comparing(task ->
                    task.getProject() != null ? task.getProject() : "");
            case "По статусу" -> Comparator.comparing(Task::isDone);
            default -> Comparator.comparing(Task::getTitle);
        };
    }

    public static void main(String[] args) {
        launch(args);
    }
}
