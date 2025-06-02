package org.example.demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.demo.domain.CurrentUser;
import org.example.demo.domain.User;
import org.example.demo.presentation.auth.AuthController;
import org.example.demo.services.AuthService;

import java.io.IOException;

public class MainApp extends Application {
    private Stage primaryStage;
    private final AuthService authService = new AuthService();

    /**
     * Запускает приложение и отображает окно авторизации.
     *
     * @param primaryStage основной стадия приложения
     * @throws IOException если произошла ошибка при загрузке FXML
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        showAuthWindow();
    }

    /**
     * Отображает окно авторизации.
     *
     * @throws IOException если произошла ошибка при загрузке FXML
     */
    public void showAuthWindow() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth_window.fxml"));
        Parent root = loader.load();

        AuthController authController = loader.getController();
        authController.setOnSuccessAuth(() -> {
            // После успешной авторизации загружаем данные пользователя
            User currentUser = CurrentUser.getInstance();
            if (currentUser != null) {
                System.out.println("Пользователь авторизован: " + currentUser.getUsername());
                System.out.println("Отображаемое имя: " + currentUser.getDisplayName());
                System.out.println("Аватар: " + currentUser.getAvatarUrl());
            }

            // Переходим в главное окно в UI потоке
            Platform.runLater(this::showMainWindow);
        });

        primaryStage.setTitle("Beresta - Авторизация");
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Отображает главное окно приложения после успешной авторизации.
     */
    public void showMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main_window.fxml"));
            Parent root = loader.load();

            // Настраиваем главное окно
            primaryStage.setTitle("Beresta - " + CurrentUser.getDisplayName());
            Scene scene = new Scene(root, 1000, 700);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.centerOnScreen();
            primaryStage.setResizable(true);

            // Закрываем окно авторизации
            primaryStage.setOnCloseRequest(e -> {
                Platform.exit();
                System.exit(0);
            });

        } catch (IOException e) {
            e.printStackTrace();
            showError("Не удалось загрузить главное окно");
        }
    }

    private void showError(String message) {
        // В реальном приложении лучше использовать Alert или другой способ показа ошибок
        System.err.println(message);
    }

    /**
     * Точка входа в приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        launch(args);
    }
}