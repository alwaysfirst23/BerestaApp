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
import org.example.demo.domain.Task;
import org.example.demo.infrastructure.DatabaseConnector;
import org.example.demo.infrastructure.DatabaseTaskRepository;
import org.example.demo.services.PomodoroTimer;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private DatabaseTaskRepository taskRepository;

    @FXML private VBox pomodoroPanel;
    @FXML private Label pomodoroTimeLabel;
    @FXML private Label pomodoroModeLabel;
    @FXML private Button startPomodoroButton;
    @FXML private Button pausePomodoroButton;
    @FXML private Button resetPomodoroButton;

    private PomodoroTimer pomodoroTimer;

    @FXML
    public void initialize() throws IOException {
        setupTopPanel();
        setupTabPane();
        setupRoundButton();
        setupMenuButton(menuButton);
        this.taskRepository = new DatabaseTaskRepository(DatabaseConnector.taskConnect());
        // Инициализация Pomodoro Timer
        initPomodoroTimer();
        // Переносим загрузку задач после полной инициализации
        Platform.runLater(() -> {
            loadTasksByProjects();
        });
    }

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

    private void togglePomodoroButtons(boolean isRunning) {
        startPomodoroButton.setDisable(isRunning);
        pausePomodoroButton.setDisable(!isRunning);
    }

    private void updatePomodoroButtons() {
        boolean isRunning = pomodoroTimer.isRunningProperty().get();
        startPomodoroButton.setDisable(isRunning);
        pausePomodoroButton.setDisable(!isRunning);

        // Стилизация кнопок
        startPomodoroButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        pausePomodoroButton.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black;");
        resetPomodoroButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
    }


    private void loadTasksByProjects() {
        try {
            // Проверка инициализации
            if (projectsContainer == null) {
                System.err.println("Error: projectsContainer is not initialized!");
                return;
            }

            // Очищаем существующие колонки
            projectsContainer.getChildren().clear();

            // Загружаем задачи из репозитория
            List<Task> tasks = taskRepository.findAll();
            Map<String, List<Task>> tasksByProject = tasks.stream()
                    .collect(Collectors.groupingBy(Task::getProject));

            // Создаем колонки для каждого проекта
            tasksByProject.forEach((projectName, projectTasks) -> {
                try {
                    // Создаем новую колонку
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(
                            "/project_column.fxml"
                    ));
                    VBox projectColumn = loader.load();
                    ProjectColumnController controller = loader.getController();

                    // Настраиваем колонку
                    controller.setProjectName(projectName);
                    controller.setTaskRepository(taskRepository);

                    // Добавляем задачи в колонку
                    projectTasks.forEach(controller::addTask);

                    // Добавляем колонку в контейнер
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

    private void setupTopPanel() {
        profileButton.getStyleClass().add("profile-button");
        profileButton.setOnAction(e -> showProfile());
        setupMenuButton(menuButton);
    }

    private void setupTabPane() {
        tabPane.getSelectionModel().selectFirst();
    }

    private void setupRoundButton() {
        addWindowButton.setOnAction(event -> showCreateWindowDialog());
    }


    private void showProfile() {
        // Логика просмотра профиля
        System.out.println("Профиль...");
    }

    private void setupMenuButton(Button menuButton) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem themeItem = new MenuItem("Тема");
        MenuItem timeItem = new MenuItem("Тайм менеджмент");
        MenuItem pomodoroSettings = new MenuItem("Настройки Pomodoro");

        themeItem.setOnAction(e -> {
            changeTheme();
            contextMenu.hide();
        });

        timeItem.setOnAction(e -> {
            showTimeManagement();
            contextMenu.hide();
        });

        pomodoroSettings.setOnAction(e -> {
            showPomodoroSettings();
            contextMenu.hide();
        });

        contextMenu.getItems().addAll(themeItem, timeItem, pomodoroSettings);
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

    private void showNotification(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Pomodoro");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initStyle(StageStyle.UTILITY);
        alert.initOwner(pomodoroTimeLabel.getScene().getWindow());
        alert.showAndWait();
    }

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

    private void addProjectColumn(String projectName) {
        try {
            // Загрузка FXML колонки
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/project_column.fxml"
            ));
            VBox newColumn = loader.load();
            ProjectColumnController controller = loader.getController();

            // Настройка поведения высоты
            newColumn.setMaxHeight(Double.MAX_VALUE);
            newColumn.minHeightProperty().bind(newColumn.prefHeightProperty());

            // Инициализация колонки
            controller.setProjectName(projectName);
            controller.setTaskRepository(taskRepository);

            // Добавление в контейнер
            projectsContainer.getChildren().add(newColumn);

            // Принудительное обновление layout
            Platform.runLater(() -> {
                projectsContainer.requestLayout();
            });

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось создать колонку проекта: " + e.getMessage());
        }
    }


    private void changeTheme() {
        // Логика смены темы
        System.out.println("Смена темы...");
    }

    private void showTimeManagement() {
        // Логика отображения окна тайм-менеджмента
        System.out.println("Тайм-менеджмент...");
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}