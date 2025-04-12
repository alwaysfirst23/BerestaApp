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

    private void loadTasksByProjects() {
        try {
            DatabaseTaskRepository repository = new DatabaseTaskRepository(DatabaseConnector.taskConnect());
            List<Task> tasks = repository.findAll();

            Map<String, List<Task>> tasksByProject = tasks.stream()
                    .collect(Collectors.groupingBy(Task::getProject));

            tasksByProject.forEach((projectName, projectTasks) -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(
                            "/project_column.fxml"
                    ));
                    VBox projectColumn = loader.load();
                    ProjectColumnController controller = loader.getController();
                    controller.setProjectName(projectName);
                    controller.setTaskRepository(repository); // Передаем репозиторий

                    projectTasks.forEach(controller::addTask);
                    projectsFlowPane.getChildren().add(projectColumn);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
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