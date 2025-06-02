package org.example.demo.presentation.main;

import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.example.demo.services.StatsDao;
import org.example.demo.domain.TaskStats;

import java.util.Map;

public class StatsTabController {
    @FXML private ProgressBar projectProgress;
    @FXML private PieChart deadlineComplianceChart;
    @FXML private PieChart taskStatusChart;
    @FXML private PieChart priorityDistributionChart;
    @FXML private PieChart taskTypeChart;
    @FXML private Button refreshButton;
    @FXML private Label progressPercent;
    @FXML private Label progressText;

    private static final String[] CUSTOM_PALETTE = {
            "#8FD3E2", "#9AD9D9", "#A0D0D0",
            "#B0EAEB", "#C1F0F0", "#D3F5F5"
    };

    @FXML
    public void initialize() {
        refreshStats();
        setupStyles();
    }

    @FXML
    public void refreshStats() {
        TaskStats stats = StatsDao.getLatestStats();
        Map<String, Integer> projectStats = StatsDao.getProjectStats();

        if (stats == null) {
            setupFallbackCharts();
            return;
        }

        updateCharts(stats, projectStats);
    }

    private void updateCharts(TaskStats stats, Map<String, Integer> projectStats) {
        // 1. Прогресс проекта
        double completion = stats.projectCompletion() / 100.0;
        projectProgress.setProgress(completion);

        // Обновляем текстовое представление
        String percentText = String.format("%.1f%%", stats.projectCompletion());
        progressPercent.setText(percentText);
        progressText.setText(percentText);

        // 2. Соблюдение дедлайнов
        deadlineComplianceChart.getData().setAll(
                new PieChart.Data("Соблюдены", stats.completedTasks() - stats.overdueTasks()),
                new PieChart.Data("Просрочены", stats.overdueTasks())
        );

        // 3. Статусы задач
        taskStatusChart.getData().setAll(
                new PieChart.Data("В работе", stats.inProgressTasks()),
                new PieChart.Data("Выполнено", stats.completedTasks()),
                new PieChart.Data("Не начато", stats.totalTasks() - stats.completedTasks() - stats.inProgressTasks())
        );

        // 4. Приоритеты задач
        priorityDistributionChart.getData().setAll(
                new PieChart.Data("Высокий", stats.highPriorityTasks()),
                new PieChart.Data("Средний", stats.mediumPriorityTasks()),
                new PieChart.Data("Низкий", stats.lowPriorityTasks())
        );

        // 5. Типы задач (проектов)
        taskTypeChart.getData().clear();
        projectStats.forEach((project, count) -> {
            taskTypeChart.getData().add(new PieChart.Data(project, count));
        });

        applyCustomColors();
    }

    // Обновите метод setupFallbackCharts
    private void setupFallbackCharts() {
        projectProgress.setProgress(0);
        progressPercent.setText("0%");
        progressText.setText("0%");

        PieChart.Data noData = new PieChart.Data("Нет данных", 1);

        deadlineComplianceChart.getData().setAll(noData);
        taskStatusChart.getData().setAll(noData);
        priorityDistributionChart.getData().setAll(noData);
        taskTypeChart.getData().setAll(noData);
    }

    private void setupStyles() {
        projectProgress.setStyle("-fx-accent: #B0EAEB;");
        styleLegends();
    }

    private void applyCustomColors() {
        colorizeChart(deadlineComplianceChart, 3, 1);
        colorizeChart(taskStatusChart, 0, 2, 4);
        colorizeChart(priorityDistributionChart, 1, 3, 5);

        // Для диаграммы проектов используем циклические цвета
        if (!taskTypeChart.getData().isEmpty()) {
            for (int i = 0; i < taskTypeChart.getData().size(); i++) {
                String color = CUSTOM_PALETTE[i % CUSTOM_PALETTE.length];
                if (taskTypeChart.getData().get(i).getNode() != null) {
                    taskTypeChart.getData().get(i).getNode().setStyle(
                            "-fx-pie-color: " + color + ";"
                    );
                }
            }
        }
    }

    private void colorizeChart(PieChart chart, int... colorIndices) {
        for (int i = 0; i < chart.getData().size(); i++) {
            String color = CUSTOM_PALETTE[colorIndices[i % colorIndices.length]];
            if (chart.getData().get(i).getNode() != null) {
                chart.getData().get(i).getNode().setStyle(
                        "-fx-pie-color: " + color + ";"
                );
            }
        }
    }

    private void styleLegends() {
        String css = String.format("""
            .chart-legend-item-symbol {
                -fx-background-radius: 8;
                -fx-padding: 4px;
                -fx-background-color: %s, %s, %s, %s, %s, %s;
            }
            """, CUSTOM_PALETTE[0], CUSTOM_PALETTE[1], CUSTOM_PALETTE[2],
                CUSTOM_PALETTE[3], CUSTOM_PALETTE[4], CUSTOM_PALETTE[5]);

        deadlineComplianceChart.setStyle(css);
        taskStatusChart.setStyle(css);
        priorityDistributionChart.setStyle(css);
        taskTypeChart.setStyle(css);
    }
}