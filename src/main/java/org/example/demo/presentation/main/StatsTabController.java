package org.example.demo.presentation.main;

import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;

public class StatsTabController {
    @FXML private ProgressBar projectProgress;
    @FXML private PieChart deadlineComplianceChart;
    @FXML private PieChart taskStatusChart;
    @FXML private PieChart priorityDistributionChart;
    @FXML private PieChart taskTypeChart;

    // Наша кастомная палитра (6 оттенков голубого)
    private static final String[] CUSTOM_PALETTE = {
            "#8FD3E2",  // dark
            "#9AD9D9",
            "#A0D0D0",
            "#B0EAEB",  // base
            "#C1F0F0",
            "#D3F5F5"   // light
    };

    @FXML
    public void initialize() {
        // Настройка ProgressBar
        projectProgress.setProgress(0.8); // 80% готовность
        projectProgress.setStyle("-fx-accent: #B0EAEB;");

        // Настройка диаграмм (прежний код)
        setupCharts();
        applyCustomColors();
        styleLegends();
    }

    private void setupCharts() {
        // Диаграмма 1: Соблюдение дедлайнов
        deadlineComplianceChart.getData().addAll(
                new PieChart.Data("Соблюдены", 85),
                new PieChart.Data("Просрочены", 15)
        );

        // Диаграмма 2: Статусы задач
        taskStatusChart.getData().addAll(
                new PieChart.Data("В работе", 20),
                new PieChart.Data("Выполнено", 80)
        );

        // Диаграмма 3: Приоритеты
        priorityDistributionChart.getData().addAll(
                new PieChart.Data("Высокий", 15),
                new PieChart.Data("Средний", 50),
                new PieChart.Data("Низкий", 35)
        );

        // Диаграмма 4: Типы задач
        taskTypeChart.getData().addAll(
                new PieChart.Data("Дизайн", 47),
                new PieChart.Data("Базы данных", 20),
                new PieChart.Data("Тестировка", 33)
        );
    }

    private void applyCustomColors() {
        // Для каждой диаграммы назначаем цвета вручную
        colorizeChart(deadlineComplianceChart, 3, 1);  // Основной и темный
        colorizeChart(taskStatusChart, 0, 2, 4);      // Темный, средний, светлый
        colorizeChart(priorityDistributionChart, 1, 3, 5);
        colorizeChart(taskTypeChart, 2, 4, 0);
    }

    private void colorizeChart(PieChart chart, int... colorIndices) {
        for (int i = 0; i < chart.getData().size(); i++) {
            String color = CUSTOM_PALETTE[colorIndices[i % colorIndices.length]];
            chart.getData().get(i).getNode().setStyle(
                    "-fx-pie-color: " + color + ";"
            );
        }
    }

    private void styleLegends() {
        String css = String.format("""
            .chart-legend-item-symbol {
                -fx-background-radius: 8;
                -fx-padding: 4px;
                -fx-background-color: %s, %s, %s, %s, %s, %s;
            }
            """,
                CUSTOM_PALETTE[0], CUSTOM_PALETTE[1], CUSTOM_PALETTE[2],
                CUSTOM_PALETTE[3], CUSTOM_PALETTE[4], CUSTOM_PALETTE[5]
        );

        deadlineComplianceChart.setStyle(css);
        taskStatusChart.setStyle(css);
        priorityDistributionChart.setStyle(css);
        taskTypeChart.setStyle(css);
    }
}