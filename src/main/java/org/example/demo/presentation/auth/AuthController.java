package org.example.demo.presentation.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import lombok.Setter;
import org.example.demo.services.AuthService;
import org.example.demo.services.DialogHelper;

public class AuthController {
    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private GridPane gridPane;

    @Setter
    private Runnable onSuccessAuth;

    private final AuthService authService = new AuthService();
    private final DialogHelper dialogHelper = new DialogHelper();

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = loginField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            dialogHelper.showErrorDialog("Ошибка", "Пожалуйста, заполните все поля");
            return;
        }

        boolean loginResult = authService.login(username, password);

        if (loginResult) {
            dialogHelper.showInfoDialog("Успешно", "Авторизация прошла успешно");
            if (onSuccessAuth != null) {
                onSuccessAuth.run();
            }
        } else {
            dialogHelper.showErrorDialog("Ошибка", "Неверный логин или пароль");
            passwordField.clear();
        }
    }

    @FXML
    public void handleRegistration(ActionEvent event) {
        String username = loginField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            dialogHelper.showErrorDialog("Ошибка", "Пожалуйста, заполните все поля");
            return;
        }

        boolean registrationResult = authService.register(username, password);

        if (registrationResult) {
            dialogHelper.showInfoDialog("Успешно", "Регистрация прошла успешно. Теперь вы можете войти.");
            passwordField.clear();
        } else {
            dialogHelper.showErrorDialog("Ошибка", "Пользователь с таким логином уже существует");
        }
    }
}