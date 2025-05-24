import org.example.demo.infrastructure.DatabaseInitializer;

public class EmptyTest implements Runnable {
    public static void main(String[] args) {
        DatabaseInitializer.authInitialize();
    }
    @Override
    public void run() {

    }
}
