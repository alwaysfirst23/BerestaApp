package org.example.demo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Task {
    private String title; // Заголовок
    private String description; // Описание
    private int priority; // Приоритет: 1-низкий, 2-средний, 3-срочно, 4-максимально срочно
    private LocalDate deadline; // Дедлайн
    private String worker; // Исполнитель
    private boolean done;

    public Task(String title, String description, int priority, LocalDate deadline, String worker) throws IncorrectTask {
        if (title == null || title.trim().isEmpty() || description == null || description.trim().isEmpty()) {
            throw new IncorrectTask("Нельзя создать задачу без названия или описания");
        }
        if (priority < 1 || priority > 4) {
            throw new IncorrectTask("Приоритет должен быть от 1 до 4");
        }
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.deadline = deadline;
        this.worker = worker;
        this.done = false;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty() || description == null || description.trim().isEmpty()){
            throw new IncorrectTask("Заметка не может быть пустой. Добавьте заголовок или описание");
        }
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setDescription(String description) {
        if (title == null || title.trim().isEmpty() || description == null || description.trim().isEmpty()){
            throw new IncorrectTask("Заметка не может быть пустой. Добавьте заголовок или описание");
        }
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setPriority(int priority) throws IllegalArgumentException {
        if (priority < 1 || priority > 4){
            throw new IllegalArgumentException("Приоритет должен быть от 1 до 4");
        }
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public String getFormattedDeadline(String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return deadline.format(formatter);
    }

    public long daysUntilDeadline() {
        LocalDate today = LocalDate.now(); // Получаем текущую дату
        return ChronoUnit.DAYS.between(today, deadline); // Вычисляем количество дней между сегодня и дедлайном
    }

    public void setWorker(String worker) {
        this.worker = worker;
    }

    public String getWorker() {
        return worker;
    }

    public void setDone(boolean done){
        this.done = done;
    }

    public boolean isDone() {
        return done;
    }

    private String whichPriority(int priority){
        return switch (priority) {
            case 1 -> "Вообще не срочно";
            case 2 -> "Не особо срочно";
            case 3 -> "Срочно";
            case 4 -> "Очень срочно!";
            default -> "ERROR";
        };
    }
}