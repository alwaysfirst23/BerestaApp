package org.example.demo.presentation.main;

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

    @FXML
    private FlowPane projectsFlowPane;

    @FXML
    public void initialize() throws IOException {
        setupTopPanel();
        setupTabPane();
        setupRoundButton();
        setupMenuButton(menuButton);
        loadTasksByProjects(); // Загружаем задачи при старте
    }

    private void loadTasksByProjects() throws IOException {
        DatabaseTaskRepository repository = new DatabaseTaskRepository(DatabaseConnector.taskConnect());
        List<Task> tasks = repository.findAll();

        Map<String, List<Task>> tasksByProject = tasks.stream()
                .collect(Collectors.groupingBy(Task::getProject));

        tasksByProject.forEach((projectName, projectTasks) -> {
            try {
                // Создаем новый FXMLLoader для каждой колонки
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/project_column.fxml"));
                VBox projectColumn = loader.load();
                ProjectColumnController controller = loader.getController();
                controller.setProjectName(projectName);

                projectTasks.forEach(controller::addTask);
                projectsFlowPane.getChildren().add(projectColumn);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private VBox createProjectColumn(String projectName, List<Task> tasks) {
        VBox column = new VBox(10); // Отступ между задачами 10px
        column.setPadding(new Insets(10));
        column.setStyle("-fx-background-color: #f0f0f0; -fx-border-radius: 5; -fx-background-radius: 5;");

        // Заголовок колонки (название проекта)
        Label projectLabel = new Label(projectName);
        projectLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        column.getChildren().add(projectLabel);

        // Добавляем задачи в колонку
        tasks.forEach(task -> {
            Node taskCard = createTaskCard(task);
            column.getChildren().add(taskCard);
        });

        return column;
    }

    private Node createTaskCard(Task task) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));

        Label titleLabel = new Label("Название: " + task.getTitle());
        Label descLabel = new Label("Описание: " + task.getDescription());
        Label deadlineLabel = new Label("Дедлайн: " + task.getDeadline());
        Label workerLabel = new Label("Исполнитель: " + task.getWorker());
        Label priorityLabel = new Label("Приоритет: " + task.whichPriority());
        Label statusLabel = new Label("Статус: " + (task.isDone() ? "Выполнено" : "В работе"));

        card.getChildren().addAll(
                titleLabel,
                descLabel,
                deadlineLabel,
                workerLabel,
                priorityLabel,
                statusLabel
        );
        return card;
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
        addWindowButton.getStyleClass().add("round-button");
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
    private void showCreateWindowDialog() {
        // Логика отображения диалога создания окна
        System.out.println("Диалог создания окна...");
    }

    private void changeTheme() {
        // Логика смены темы
        System.out.println("Смена темы...");
    }

    private void showTimeManagement() {
        // Логика отображения окна тайм-менеджмента
        System.out.println("Тайм-менеджмент...");
    }
}