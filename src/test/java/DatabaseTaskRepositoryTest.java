
import org.example.demo.domain.Task;
import org.example.demo.infrastructure.DatabaseConnector;
import org.example.demo.infrastructure.DatabaseTaskRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseTaskRepositoryTest {
    private static Connection connection = DatabaseConnector.taskConnect();
    private static DatabaseTaskRepository repository = new DatabaseTaskRepository(connection);


    @Test
    void findAll_ShouldReturnTasksFromDatabase() {
        // Act
        List<Task> tasks = repository.findAll();

        // Assert
        assertNotNull(tasks, "Список задач не должен быть null");
        assertFalse(tasks.isEmpty(), "Список задач не должен быть пустым");

        // Выводим задачи в консоль
        System.out.println("\nНайденные задачи в базе данных:");
        System.out.println("--------------------------------");
        tasks.forEach(task -> {
            System.out.println("ID: " + task.getId());
            System.out.println("Название: " + task.getTitle());
            System.out.println("Описание: " + task.getDescription());
            System.out.println("Проект: " + task.getProject());
            System.out.println("Дедлайн: " + task.getDeadline());
            System.out.println("Исполнитель: " + task.getWorker());
            System.out.println("Приоритет: " + task.getPriority());
            System.out.println("Выполнено: " + task.isDone());
            System.out.println("--------------------------------");
        });
    }

    @Test
    void save_ShouldInsertTaskAndSetGeneratedId() {
        // Arrange
        Task task = new Task(
                "Test Title",
                "Test Description",
                2,
                LocalDate.of(2025, 12, 31),
                "Test Worker"
        );
        task.setProject("Coding");
        task.setDone(false);

        // Act
        repository.save(task);

        // Assert
        assertTrue(task.getId() > 0, "ID задачи должен быть установлен");
    }

}