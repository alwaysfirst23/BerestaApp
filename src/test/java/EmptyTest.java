import org.example.demo.infrastructure.DatabaseConnector;
import org.example.demo.infrastructure.DatabaseInitializer;

public class EmptyTest implements Runnable {
    public static void main(String[] args) {

        // Инициализируем только таблицу task_stats
        DatabaseInitializer.statsInitializeBasic();
        // Проверяем, что данные есть
        try (var conn = DatabaseConnector.taskConnect();
             var stmt = conn.createStatement()) {

            // Выводим последнюю запись статистики
            var rs = stmt.executeQuery(
                    "SELECT * FROM task_stats ORDER BY timestamp DESC LIMIT 1");

            if (rs.next()) {
                System.out.println("Текущая статистика:");
                System.out.println("Всего задач: " + rs.getInt("total_tasks"));
                System.out.println("Выполнено: " + rs.getInt("completed_tasks"));
                System.out.println("Просрочено: " + rs.getInt("overdue_tasks"));
                System.out.println("В работе: " + rs.getInt("in_progress_tasks"));
                System.out.println("Прогресс: " + rs.getDouble("project_completion") + "%");
            } else {
                System.out.println("Нет данных статистики");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

    }
}
