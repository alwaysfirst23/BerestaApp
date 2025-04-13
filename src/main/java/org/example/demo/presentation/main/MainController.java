package org.example.demo.presentation.main;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.example.demo.domain.Task;
import org.example.demo.infrastructure.DatabaseConnector;
import org.example.demo.infrastructure.DatabaseTaskRepository;

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



    @FXML
    public void initialize() throws IOException {
        setupTopPanel();
        setupTabPane();
        setupRoundButton();
        setupMenuButton(menuButton);
        this.taskRepository = new DatabaseTaskRepository(DatabaseConnector.taskConnect());
        // Переносим загрузку задач после полной инициализации
        Platform.runLater(() -> {
            loadTasksByProjects();
        });
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

        themeItem.setOnAction(e -> {
            changeTheme();
            contextMenu.hide();
        });

        timeItem.setOnAction(e -> {
            showTimeManagement();
            contextMenu.hide();
        });

        contextMenu.getItems().addAll(themeItem, timeItem);
        menuButton.setOnMouseClicked(e -> {
            contextMenu.show(menuButton, e.getScreenX(), e.getScreenY());
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