package org.example.demo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Getter
public class Task {
    private String title; // Заголовок
    private String description; // Описание
    private int priority; // Приоритет: 1-низкий, 2-средний, 3-срочно, 4-максимально срочно
    @Setter
    private LocalDate deadline; // Дедлайн
    @Setter
    private String worker; // Исполнитель
    @Setter
    private boolean done;
    @Setter
    @Getter
    private String project;

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

    public void setDescription(String description) {
        if (title == null || title.trim().isEmpty() || description == null || description.trim().isEmpty()){
            throw new IncorrectTask("Заметка не может быть пустой. Добавьте заголовок или описание");
        }
        this.description = description;
    }

    public void setPriority(int priority) throws IllegalArgumentException {
        if (priority < 1 || priority > 4){
            throw new IllegalArgumentException("Приоритет должен быть от 1 до 4");
        }
        this.priority = priority;
    }

    /**
     * Форматирует дату, приводя к String
     * @param pattern дата
     * @return строка
     */
    public String getFormattedDeadline(String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return deadline.format(formatter);
    }

    public long daysUntilDeadline() {
        LocalDate today = LocalDate.now(); // Получаем текущую дату
        return ChronoUnit.DAYS.between(today, deadline); // Вычисляем количество дней между сегодня и дедлайном
    }

    /**
     * Возвращает текстовую расшифровку числового приоритета
     * @param priority приоритет
     * @return текст
     */
    public String whichPriority(int priority){
        return switch (priority) {
            case 1 -> "Не срочно";
            case 2 -> "Средне";
            case 3 -> "Срочно";
            case 4 -> "Очень срочно!";
            default -> "ERROR";
        };
    }
}