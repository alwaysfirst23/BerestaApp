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

    // Метод для добавления задачи
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

    // Метод для удаления задачи по индексу
    public void removeTask(int index) {
        if (index > 0 && index <= task_list.size()) {
            task_list.remove(index - 1);
        } else {
            System.out.println("Задача с таким индексом не найдена.");
        }
    }

    // Метод для редактирования задачи по индексу
    public void editTask(int index, String title, String description, int priority, LocalDate deadline, String worker) {
        if (index >= 0 && index < task_list.size()) { // индексация с нуля
            Task task = task_list.get(index);
            if (!(title == null || title.trim().isEmpty())) task.setTitle(title);
            if (!(description == null || description.trim().isEmpty()))
                task.setDescription(description); // Добавить проверку на непустоту
            if (priority != 0) task.setPriority(priority);
            if (!(deadline == null)) task.setDeadline(deadline);
            if (!(worker == null || worker.trim().isEmpty())) task.setWorker(worker);
        } else {
            System.out.println("Задача с таким индексом не найдена.");
        }
    }

    // Метод для вывода всех задач
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

    public void editDone(int index) throws IncorrectTask {
        if (index < 1 || index > task_list.size()) {
            throw new IncorrectTask("Задача с таким индексом не найдена");
        }
        task_list.get(index - 1).setDone(true);
    }

    public LinkedList<Task> getTasks() {
        return task_list;
    }
}