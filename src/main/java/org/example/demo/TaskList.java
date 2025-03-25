package org.example.demo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

public class TaskList {
    private LinkedList<Task> task_list;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public TaskList() {
        task_list = new LinkedList<>();
    }

    /**
     * Добавляет новую задачу в список
     * @param title заголовок
     * @param description описание задачи
     * @param priority приоритет (1-4)
     * @param date дедлайн
     * @param worker исполнитель
     * @throws IncorrectTask если некорректно заполнены поля
     */
    public void createTask(String title, String description,
                           int priority, LocalDate date, String worker) throws IncorrectTask {

        Task task;
        if (worker == null || worker.trim().isEmpty()) {
            task = new Task(title, description, priority, date, "Я");
        } else {
            task = new Task(title, description, priority, date, worker);
        }
        task_list.add(task);
    }

    /**
     * Удаляет задачу с указанным номером
     * @param index номер задачи
     */
    public void removeTask(int index) {
        if (index > 0 && index <= task_list.size()) {
            task_list.remove(index - 1);
        } else {
            System.out.println("Задача с таким индексом не найдена.");
        }
    }

    /**
     * Редактирует задачу по индексу
     * @param index номер задачи
     * @param title новый заголовок
     * @param description новое описание
     * @param priority новый приоритет
     * @param deadline новый дедлайн
     * @param worker новый исполнитель
     */
    public void editTask(int index, String title, String description, int priority, LocalDate deadline, String worker) {
        if (index > 0 && index <= task_list.size()) { // индексация с нуля
            Task task = task_list.get(index-1);
            if (!(title == null || title.trim().isEmpty())) task.setTitle(title);
            if (!(description == null || description.trim().isEmpty()))  task.setDescription(description); // Добавить проверку на непустоту
            if (priority > 0 && priority < 5) task.setPriority(priority);
            if (!(deadline == null)) task.setDeadline(deadline);
            if (!(worker == null || worker.trim().isEmpty())) task.setWorker(worker);
        } else {
            System.out.println("Задача с таким индексом не найдена.");
        }
    }

    /**
     * Возвращает список всех задач
     * @return список задач
     */
    public String printAllTasks() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < task_list.size(); i++) {
            Task task = task_list.get(i);
            sb.append(i + 1)
                    .append(". [")
                    .append(task.isDone() ? "✓" : " ")
                    .append("] ")
                    .append(task.getTitle())
                    .append("\n   Описание: ")
                    .append(task.getDescription())
                    .append("\n   Приоритет: ")
                    .append(task.getPriority())
                    .append("\n   Дедлайн: ")
                    .append(task.getDeadline() != null
                            ? task.getDeadline().format(dateFormatter)
                            : "нет")
                    .append("\n   Исполнитель: ")
                    .append(task.getWorker().isEmpty() ? "я" : task.getWorker())
                    .append("\n\n");
        }
        return sb.toString().isEmpty() ? "Список задач пуст" : sb.toString();
    }

    public LinkedList<Task> getTasks() {
        return task_list;
    }
}