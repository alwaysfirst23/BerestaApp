package org.example.demo.domain;

import lombok.Getter;
import lombok.Setter;
import org.example.demo.domain.exceptions.IncorrectTask;

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
    @Setter
    @Getter
    private int id;

    /**
     * Конструктор для создания новой задачи.
     *
     * @param title Заголовок задачи.
     * @param description Описание задачи.
     * @param priority Приоритет задачи (от 1 до 4).
     * @param deadline Дата дедлайна задачи.
     * @param worker Исполнитель задачи.
     * @throws IncorrectTask Если заголовок или описание пусты, либо приоритет вне допустимого диапазона.
     */
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

    /**
     * Устанавливает заголовок задачи.
     *
     * @param title Заголовок задачи.
     * @throws IncorrectTask Если заголовок или описание пусты.
     */
    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty() || description == null || description.trim().isEmpty()){
            throw new IncorrectTask("Заметка не может быть пустой. Добавьте заголовок или описание");
        }
        this.title = title;
    }

    /**
     * Устанавливает описание задачи.
     *
     * @param description Описание задачи.
     * @throws IncorrectTask Если заголовок или описание пусты.
     */
    public void setDescription(String description) {
        if (title == null || title.trim().isEmpty() || description == null || description.trim().isEmpty()){
            throw new IncorrectTask("Заметка не может быть пустой. Добавьте заголовок или описание");
        }
        this.description = description;
    }

    /**
     * Устанавливает приоритет задачи.
     *
     * @param priority Приоритет задачи (от 1 до 4).
     * @throws IllegalArgumentException Если приоритет вне допустимого диапазона.
     */
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

    /**
     * Вычисляет количество дней до дедлайна.
     *
     * @return Количество дней до дедлайна.
     */
    public long daysUntilDeadline() {
        LocalDate today = LocalDate.now(); // Получаем текущую дату
        return ChronoUnit.DAYS.between(today, deadline); // Вычисляем количество дней между сегодня и дедлайном
    }

    /**
     * Возвращает текстовую расшифровку числового приоритета
     * @return текст
     */
    public String whichPriority(){
        return switch (this.priority) {
            case 1 -> "Не срочно";
            case 2 -> "Средне";
            case 3 -> "Срочно";
            case 4 -> "Очень срочно!";
            default -> "ERROR";
        };
    }
}