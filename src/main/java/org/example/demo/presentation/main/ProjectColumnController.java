package org.example.demo.presentation.main;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import lombok.Setter;
import org.example.demo.domain.Task;
import org.example.demo.domain.exceptions.IncorrectTask;
import org.example.demo.infrastructure.DatabaseTaskRepository;
import org.example.demo.presentation.TaskDialog;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class ProjectColumnController {
    @FXML private VBox projectColumn;
    @FXML private Label projectNameLabel;
    @FXML private VBox tasksContainer;
    @FXML private Button addTaskButton;

    private String projectName;
    @Setter
    private DatabaseTaskRepository taskRepository;

    public void setProjectName(String name) {
        this.projectName = name;
        projectNameLabel.setText(name);
    }

    @FXML
    public void handleAddTask() {
        TaskDialog dialog = new TaskDialog(projectName);
        Optional<Task> result = dialog.showAndWait();

        result.ifPresent(task -> {
            try {
                // Сохраняем в БД
                taskRepository.save(task);
                // Добавляем в UI
                addTask(task);
            } catch (Exception e) {
                showErrorAlert("Ошибка сохранения задачи", e.getMessage());
            }
        });
    }

    public void addTask(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/task_card.fxml"));
            VBox taskCard = loader.load();
            TaskCardController controller = loader.getController();

            controller.setTask(
                    task,
                    () -> { // onTaskDone
                        try {
                            task.setDone(true);
                            taskRepository.update(task);
                            controller.updateUI();
                        } catch (Exception e) {
                            showErrorAlert("Ошибка", "Не удалось обновить задачу");
                            e.printStackTrace();
                        }
                    },
                    () -> { // onDeleteTask
                        try {
                            taskRepository.delete(task.getId());
                            tasksContainer.getChildren().remove(taskCard);
                        } catch (Exception e) {
                            showErrorAlert("Ошибка", "Не удалось удалить задачу");
                            e.printStackTrace();
                        }
                    },
                    () -> { // onEditTask
                        TaskDialog editDialog = new TaskDialog(task.getProject(), task);
                        Optional<Task> result = editDialog.showAndWait();
                        result.ifPresent(updatedTask -> {
                            try {
                                // Обновляем ВСЕ поля задачи
                                task.setTitle(updatedTask.getTitle());
                                task.setDescription(updatedTask.getDescription());
                                task.setPriority(updatedTask.getPriority());
                                task.setDeadline(updatedTask.getDeadline());
                                task.setWorker(updatedTask.getWorker());

                                // Явно обновляем в БД
                                taskRepository.update(task);

                                // Обновляем UI
                                controller.updateUI();

                            } catch (Exception e) {
                                showErrorAlert("Ошибка", "Не удалось обновить задачу");
                                e.printStackTrace();
                            }
                        });
                    }
            );

            tasksContainer.getChildren().add(taskCard);
        } catch (IOException e) {
            showErrorAlert("Ошибка", "Не удалось загрузить карточку задачи");
            e.printStackTrace();
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}