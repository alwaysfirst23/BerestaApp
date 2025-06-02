package org.example.demo.presentation.main;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.example.demo.domain.Task;
import org.example.demo.infrastructure.DatabaseConnector;
import org.example.demo.infrastructure.DatabaseTaskRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

public class TasksTabController {
    @FXML private Pane chartContainer;
    @FXML private Label emptyLabel;
    @FXML private Button refreshButton;

    private DatabaseTaskRepository taskRepository;
    private static final Color TASK_COLOR = Color.web("#B0EAEB");
    private static final Color BACKGROUND_COLOR = Color.web("#313333");
    private static final Color TEXT_COLOR = Color.web("#B0EAEB");

    /**
     * Инициализация контроллера.
     */
    public void initialize() {
        this.taskRepository = new DatabaseTaskRepository(DatabaseConnector.taskConnect());
        refreshButton.setOnAction(e -> updateChart());
        updateChart();
    }

    /**
     * Обновляет график задач.
     */
    public void updateChart() {
        chartContainer.getChildren().clear();

        List<Task> tasks = taskRepository.findAll()
                .stream()
                .filter(task -> task.getDeadline() != null)
                .sorted(Comparator.comparing(Task::getDeadline))
                .toList();

        if (tasks.isEmpty()) {
            emptyLabel.setVisible(true);
            return;
        }

        emptyLabel.setVisible(false);

        LocalDate minDate = tasks.stream()
                .map(Task::getDeadline)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now())
                .minusDays(2);

        LocalDate maxDate = tasks.stream()
                .map(Task::getDeadline)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now())
                .plusDays(2);

        long totalDays = ChronoUnit.DAYS.between(minDate, maxDate) + 1;
        int daysWidth = 50;
        int rowHeight = 30;

        // Увеличиваем высоту на 20 пикселей для смещения оси X
        int chartWidth = (int) (totalDays * daysWidth) + 100;
        int chartHeight = tasks.size() * rowHeight + 70; // Было 50, увеличиваем на 20
        chartContainer.setMinSize(chartWidth, chartHeight);
        chartContainer.setPrefSize(chartWidth, chartHeight);

        drawAxes(minDate, maxDate, tasks.size(), daysWidth, rowHeight);

        for (int i = 0; i < tasks.size(); i++) {
            drawTask(tasks.get(i), i, minDate, daysWidth, rowHeight);
        }
    }

    /**
     * Рисует оси графика.
     *
     * @param minDate минимальная дата для отображения
     * @param maxDate максимальная дата для отображения
     * @param taskCount количество задач для отображения
     * @param daysWidth ширина одного дня в пикселях
     * @param rowHeight высота строки для задач в пикселях
     */
    private void drawAxes(LocalDate minDate, LocalDate maxDate, int taskCount, int daysWidth, int rowHeight) {
        // Ось Y (задачи)
        Text yAxisLabel = new Text("Задачи");
        yAxisLabel.setFill(TEXT_COLOR);
        yAxisLabel.setX(10);
        yAxisLabel.setY(20);
        chartContainer.getChildren().add(yAxisLabel);

        // Ось X (даты) - смещаем ниже
        int xAxisYPosition = taskCount * rowHeight + 60; // Было 40, увеличиваем на 20

        Text xAxisLabel = new Text("Дни");
        xAxisLabel.setFill(TEXT_COLOR);
        xAxisLabel.setX(100 + (ChronoUnit.DAYS.between(minDate, maxDate) * daysWidth) / 2);
        xAxisLabel.setY(xAxisYPosition + 10); // Подписываем ниже оси
        chartContainer.getChildren().add(xAxisLabel);

        // Разметка дат (только числа)
        LocalDate current = minDate;
        int xPos = 100;
        while (!current.isAfter(maxDate)) {
            Text dateLabel = new Text(String.valueOf(current.getDayOfMonth()));
            dateLabel.setFill(TEXT_COLOR);
            dateLabel.setX(xPos);
            dateLabel.setY(xAxisYPosition);
            chartContainer.getChildren().add(dateLabel);

            // Линия сетки - удлиняем до новой позиции оси
            Line gridLine = new Line(xPos, 30, xPos, xAxisYPosition - 5);
            gridLine.setStroke(Color.GRAY);
            chartContainer.getChildren().add(gridLine);

            current = current.plusDays(1);
            xPos += daysWidth;
        }
    }

    /**
     * Рисует полосу задачи на графике.
     *
     * @param task задача, которую необходимо отобразить
     * @param rowIndex индекс строки для размещения полосы задачи
     * @param minDate минимальная дата для определения положения задачи
     * @param daysWidth ширина одного дня в пикселях
     * @param rowHeight высота строки для размещения задачи в пикселях
     */
    private void drawTask(Task task, int rowIndex, LocalDate minDate, int daysWidth, int rowHeight) {
        int yPos = 40 + rowIndex * rowHeight;

        // Название задачи
        Text taskLabel = new Text(task.getTitle());
        taskLabel.setFill(TEXT_COLOR);
        taskLabel.setX(10);
        taskLabel.setY(yPos + 15);
        chartContainer.getChildren().add(taskLabel);

        // Полоса задачи
        long daysFromStart = ChronoUnit.DAYS.between(minDate, task.getDeadline());
        int xPos = 100 + (int) (daysFromStart * daysWidth) - 20;

        Rectangle taskBar = new Rectangle(40, 20, TASK_COLOR);
        taskBar.setX(xPos);
        taskBar.setY(yPos);

        // Tooltip с информацией
        Tooltip tooltip = new Tooltip(
                "Задача: " + task.getTitle() + "\n" +
                        "День: " + task.getDeadline().getDayOfMonth() + "\n" +
                        "Проект: " + task.getProject()
        );
        Tooltip.install(taskBar, tooltip);

        chartContainer.getChildren().add(taskBar);
    }
}