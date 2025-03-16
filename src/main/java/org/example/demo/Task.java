package org.example.demo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Task {
    private String title; // Заголовок
    private String description; // Описание
    private int priority; // Приоритет: 0-низкий, 1-средний, 2-срочно, 3-максимально срочно
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
        if (priority < 0 || priority > 3) {
            throw new IllegalArgumentException("Приоритет должен быть от 1 до 4");
        }
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public String getFormattedDeadline(String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return deadline.format(formatter);
    }

    public long daysUntilDeadline() {
        LocalDate today = LocalDate.now(); // Получаем текущую дату
        return ChronoUnit.DAYS.between(today, deadline); // Вычисляем количество дней между сегодня и дедлайном
    }

    public void print() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy"); // Создаем форматтер для даты
        System.out.printf("%-25s %s%n", "Заголовок", title);
        System.out.printf("%-25s %s%n", "Описание задачи", description);
        System.out.printf("%-25s %s (%s)%n", "Приоритет", priority, whichPriority(priority));
        System.out.printf("%-25s %s%n", "Дедлайн", deadline.format(formatter));
        long days_left = daysUntilDeadline();
        if (days_left >= 0) {
            System.out.printf("%-25s %s%n", "Дней до дедлайна", days_left);
        } else {
            System.out.printf("%-25s %s%n", "Просрочено на (дней)", Math.abs(days_left));
        }
        System.out.printf("%-25s %s%n", "Исполнитель", worker);
        if (done){
            System.out.printf("%-25s %s%n", "Статус: ", "Выполнено");
        } else {
            System.out.printf("%-25s %s%n", "Статус: ", "В процессе");
        }
    }


    private String whichPriority(int priority){
        switch (priority){
            case 1: return "Вообще не срочно";
            case 2: return "Не особо срочно";
            case 3: return "Срочно";
            case 4: return "Очень срочно!";
        }
        return "ERROR";
    }

    public void setDone(boolean done){
        this.done = done;
    }

    public boolean isDone() {
        return done;
    }

    public void setWorker(String worker) {
        this.worker = worker;
    }

    public String getWorker() {
        return worker;
    }
}