package org.example.demo.domain;

public record TaskStats(
        int totalTasks,
        int completedTasks,
        int overdueTasks,
        int inProgressTasks,
        int highPriorityTasks,
        int mediumPriorityTasks,
        int lowPriorityTasks,
        double projectCompletion
) {}

