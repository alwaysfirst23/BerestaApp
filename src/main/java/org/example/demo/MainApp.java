package org.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.demo.presentation.auth.AuthController;

import java.io.IOException;

public class MainApp extends Application {
    private Stage primaryStage;

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
        authController.setOnSuccessAuth(this::showMainWindow);

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

            primaryStage.setTitle("Beresta");
            Scene scene = new Scene(root, 1000, 700);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.centerOnScreen();
            primaryStage.setResizable(true);
        } catch (IOException e) {
            e.printStackTrace();
            // Здесь можно добавить обработку ошибки
        }
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