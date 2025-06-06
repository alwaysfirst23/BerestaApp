package org.example.demo.presentation.main;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import org.example.demo.domain.CurrentUser;
import org.example.demo.domain.Task;
import org.example.demo.infrastructure.DatabaseConnector;
import org.example.demo.infrastructure.DatabaseSubtaskRepository;
import org.example.demo.infrastructure.DatabaseTaskRepository;
import org.example.demo.services.PomodoroTimer;
import org.example.demo.services.TaskService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Контроллер главного окна приложения.
 * Отвечает за управление элементами интерфейса и бизнес-логикой.
 */
public class MainController {

    @FXML
    private HBox topPanel;

    @FXML
    private Button profileButton;

    @FXML
    private TextField searchField;

    @FXML
    private Button menuButton;

    @FXML
    private TabPane tabPane;

    @FXML
    private StackPane bottomRightButton;

    @FXML
    private Button addWindowButton;

    @FXML private HBox projectsContainer; // Новое имя для HBox
    private TaskService taskService;

    @FXML private VBox pomodoroPanel;
    @FXML private Label pomodoroTimeLabel;
    @FXML private Label pomodoroModeLabel;
    @FXML private Button startPomodoroButton;
    @FXML private Button pausePomodoroButton;
    @FXML private Button resetPomodoroButton;

    private PomodoroTimer pomodoroTimer;

    // Отображаем данные пользователя
    String username = CurrentUser.getUsername();
    String displayName = CurrentUser.getDisplayName();
    String avatarUrl = CurrentUser.getAvatarUrl();

    /**
     * Метод инициализации контроллера.
     * Настраивает элементы интерфейса и загружает данные задач.
     *
     * @throws IOException если возникает ошибка при загрузке ресурсов
     */
    @FXML
    public void initialize() throws IOException {
        System.out.println("Текущий пользователь: " + displayName);
        System.out.println("Аватар: " + avatarUrl);
        setupTopPanel();
        setupTabPane();
        setupRoundButton();
        setupMenuButton(menuButton);
        this.taskService = new TaskService(DatabaseConnector.taskConnect());
        // Инициализация Pomodoro Timer
        initPomodoroTimer();
        // Переносим загрузку задач после полной инициализации
        Platform.runLater(() -> {
            loadTasksByProjects();
        });
        tabPane.getSelectionModel().selectFirst();

        // Загружаем контент для вкладки "Мои задачи"
        Tab tasksTab = tabPane.getTabs().get(1);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tasks_tab.fxml"));
            Pane tasksContent = loader.load();
            tasksTab.setContent(tasksContent);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить вкладку задач");
        }
        loadStatsTab();

        // Добавляем вкладку профиля
        Tab profileTab = new Tab("Профиль");
        profileTab.setClosable(false);
        tabPane.getTabs().add(profileTab);

        // Загружаем вкладку профиля
        loadProfileTab();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile_tab.fxml"));
            profileTab.setContent(loader.load());
        } catch (IOException e) {
            profileTab.setContent(new Label("Не удалось загрузить профиль"));
            e.printStackTrace();
        }
    }

    /**
     * Инициализирует таймер Помодора и настраивает обработчики событий для кнопок.
     *
     * @throws Exception если возникает ошибка при инициализации таймера
     */
    private void initPomodoroTimer() {
        try {
            pomodoroTimer = new PomodoroTimer(25, 5);

            // Привязка только к pomodoroTimeLabel (без pomodoroModeLabel)
            pomodoroTimeLabel.textProperty().bind(pomodoroTimer.remainingTimeProperty());
            setupPomodoroNotifications();

            // Изменение цвета при смене режима
            pomodoroTimer.remainingTimeProperty().addListener((obs, oldVal, newVal) -> {
                String color = "#B0EAEB";
                pomodoroTimeLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 18px; -fx-font-weight: bold;");
            });

            // Настройка кнопок
            startPomodoroButton.setOnAction(e -> {
                pomodoroTimer.start();
                togglePomodoroButtons(true);
            });

            pausePomodoroButton.setOnAction(e -> {
                pomodoroTimer.pause();
                togglePomodoroButtons(false);
            });

            resetPomodoroButton.setOnAction(e -> {
                pomodoroTimer.reset();
                togglePomodoroButtons(false);
            });

            // Первоначальная настройка кнопок
            togglePomodoroButtons(false);
        } catch (Exception e) {
            System.err.println("Ошибка инициализации Pomodoro Timer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Загружает вкладку статистики.
     *
     * @throws IOException если не удается загрузить ресурс FXML
     */
    private void loadStatsTab() {
        Tab statsTab = tabPane.getTabs().get(2); // 3-я вкладка
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/stats_tab.fxml"));
            statsTab.setContent(loader.load());
        } catch (IOException e) {
            statsTab.setContent(new Label("Вкладка статистики не загружена"));
        }
    }

    private void loadProfileTab() {
        Tab profileTab = tabPane.getTabs().stream()
                .filter(tab -> "Профиль".equals(tab.getText()))
                .findFirst()
                .orElse(null);

        if (profileTab != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile_tab.fxml"));
                profileTab.setContent(loader.load());
            } catch (IOException e) {
                profileTab.setContent(new Label("Не удалось загрузить профиль"));
                e.printStackTrace();
            }
        }
    }

    /**
     * Включает или отключает кнопки управления таймером.
     *
     * @param isRunning флаг, указывающий, запущен ли таймер
     */
    private void togglePomodoroButtons(boolean isRunning) {
        startPomodoroButton.setDisable(isRunning);
        pausePomodoroButton.setDisable(!isRunning);
    }

    /**
     * Обновляет состояние кнопок в зависимости от статуса таймера.
     */
    private void updatePomodoroButtons() {
        boolean isRunning = pomodoroTimer.isRunningProperty().get();
        startPomodoroButton.setDisable(isRunning);
        pausePomodoroButton.setDisable(!isRunning);

        // Стилизация кнопок
        startPomodoroButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        pausePomodoroButton.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black;");
        resetPomodoroButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
    }


    /**
     * Загружает задачи по проектам и обновляет контейнер с проектами.
     *
     * @throws Exception если возникает ошибка при загрузке задач
     */
    private void loadTasksByProjects() {
        try {
            projectsContainer.getChildren().clear();

            List<Task> tasks = taskService.findAllTasks(); // Используем метод сервиса
            Map<String, List<Task>> tasksByProject = tasks.stream()
                    .filter(task -> !taskService.isSubtask(task.getId())) // Исключаем подзадачи
                    .collect(Collectors.groupingBy(Task::getProject));

            tasksByProject.forEach((projectName, projectTasks) -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/project_column.fxml"));
                    VBox projectColumn = loader.load();
                    ProjectColumnController controller = loader.getController();

                    controller.setProjectName(projectName);
                    controller.setTaskService(taskService); // Передаем сервис вместо репозитория

                    projectTasks.forEach(controller::addTask);
                    projectsContainer.getChildren().add(projectColumn);

                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert("Ошибка", "Не удалось загрузить колонку проекта: " + projectName);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка загрузки", "Не удалось загрузить задачи: " + e.getMessage());
        }
    }

    /**
     * Настраивает верхнюю панель интерфейса, включая кнопку профиля.
     */
    private void setupTopPanel() {
        profileButton.getStyleClass().add("profile-button");
        profileButton.setOnAction(e -> showProfile());
        setupMenuButton(menuButton);
    }

    /**
     * Настраивает панель вкладок, устанавливая первую вкладку активной.
     */
    private void setupTabPane() {
        tabPane.getSelectionModel().selectFirst();
    }

    /**
     * Устанавливает обработчик для открытия диалога создания нового окна.
     */
    private void setupRoundButton() {
        addWindowButton.setOnAction(event -> showCreateWindowDialog());
    }


    /**
     * Отображает профиль пользователя
     */
    private void showProfile() {
        // Переключаемся на вкладку профиля (последняя вкладка)
        tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1);
    }

    /**
     * @param menuButton кнопка для настройки контекстного меню
     */
    private void setupMenuButton(Button menuButton) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem themeItem = new MenuItem("Тема");
        MenuItem pomodoroSettings = new MenuItem("Настройки Pomodoro");

        themeItem.setOnAction(e -> {
            changeTheme();
            contextMenu.hide();
        });

        pomodoroSettings.setOnAction(e -> {
            showPomodoroSettings();
            contextMenu.hide();
        });

        contextMenu.getItems().addAll(themeItem, pomodoroSettings);
        menuButton.setOnMouseClicked(e -> {
            contextMenu.show(menuButton, e.getScreenX(), e.getScreenY());
        });
    }

    // В классе MainController добавляем:
    private void setupPomodoroNotifications() {
        pomodoroTimer.setOnModeChanged(isWorkTime -> {
            Platform.runLater(() -> {
                String message = isWorkTime ? "Время работать!" : "Пора отдыхать!";
                showNotification(message);
            });
        });
    }

    /**
     * Показывает уведомление с указанным сообщением.
     *
     * @param message сообщение, отображаемое в уведомлении
     */
    private void showNotification(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Pomodoro");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initStyle(StageStyle.UTILITY);
        alert.initOwner(pomodoroTimeLabel.getScene().getWindow());
        alert.showAndWait();
    }

    /**
     * Отображает диалоговые настройки для таймера Pomodoro,
     * позволяя пользователю указать время работы и отдыха.
     */
    @FXML
    private void showPomodoroSettings() {
        // Создаем диалоговое окно
        Dialog<Pair<Integer, Integer>> dialog = new Dialog<>();
        dialog.setTitle("Настройки Pomodoro");
        dialog.setHeaderText("Укажите время работы и отдыха (в минутах):");

        // Устанавливаем кнопки
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Создаем поля ввода
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField workField = new TextField(String.valueOf(pomodoroTimer.getWorkDuration()));
        TextField breakField = new TextField(String.valueOf(pomodoroTimer.getBreakDuration()));

        grid.add(new Label("Работа (мин):"), 0, 0);
        grid.add(workField, 1, 0);
        grid.add(new Label("Отдых (мин):"), 0, 1);
        grid.add(breakField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Преобразуем результат
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    int work = Integer.parseInt(workField.getText());
                    int rest = Integer.parseInt(breakField.getText());
                    return new Pair<>(work, rest);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        // Обработка результата
        Optional<Pair<Integer, Integer>> result = dialog.showAndWait();
        result.ifPresent(settings -> {
            pomodoroTimer.setDurations(settings.getKey(), settings.getValue());
            pomodoroTimer.reset();
        });
    }

    /**
     * Отображает диалог для создания нового проекта,
     * позволяя пользователю ввести название проекта.
     */
    @FXML
    public void showCreateWindowDialog() {
        TextInputDialog dialog = new TextInputDialog("Новый проект");
        dialog.setTitle("Создание проекта");
        dialog.setHeaderText("Введите название проекта");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(projectName -> {
            if (!projectName.trim().isEmpty()) {
                addProjectColumn(projectName);
            }
        });
    }

    /**
     * Добавляет новую колонку проекта в контейнер проектов.
     *
     * @param projectName название проекта для новой колонки
     */
    private void addProjectColumn(String projectName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/project_column.fxml"));
            VBox newColumn = loader.load();
            ProjectColumnController controller = loader.getController();

            newColumn.setMaxHeight(Double.MAX_VALUE);
            newColumn.minHeightProperty().bind(newColumn.prefHeightProperty());

            controller.setProjectName(projectName);
            controller.setTaskService(taskService); // Передаем сервис вместо репозитория

            projectsContainer.getChildren().add(newColumn);
            Platform.runLater(() -> projectsContainer.requestLayout());

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось создать колонку проекта: " + e.getMessage());
        }
    }

    private void changeTheme() {
        // Логика смены темы
        System.out.println("Смена темы...");
    }

    /**
     * Отображает алерт с указанным заголовком и сообщением.
     *
     * @param title заголовок алерта
     * @param message сообщение для отображения
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}