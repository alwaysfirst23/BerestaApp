package org.example.demo.presentation.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import lombok.Setter;
import org.example.demo.domain.CurrentUser;
import org.example.demo.domain.User;
import org.example.demo.services.AuthService;
import org.example.demo.services.DialogHelper;

/**
 * Контроллер для аутентификации пользователя.
 * Обрабатывает логику входа и регистрации пользователей.
 */
public class AuthController {
    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private GridPane gridPane;

    @Setter
    private Runnable onSuccessAuth;

    private final AuthService authService = new AuthService();
    private final DialogHelper dialogHelper = new DialogHelper();

    /**
     * Обрабатывает событие входа пользователя.
     * Проверяет введенные логин и пароль, выполняет аутентификацию
     * и показывает соответствующие сообщения об успехе или ошибке.
     *
     * @param event событие нажатия кнопки входа
     */
    @FXML
    public void handleLogin(ActionEvent event) {
        String username = loginField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            dialogHelper.showErrorDialog("Ошибка", "Пожалуйста, заполните все поля");
            return;
        }

        User authenticatedUser = authService.login(username, password);

        if (authenticatedUser != null) {
            // Сохраняем данные пользователя
            CurrentUser.setInstance(authenticatedUser);

            dialogHelper.showInfoDialog("Успешно", "Авторизация прошла успешно");
            if (onSuccessAuth != null) {
                onSuccessAuth.run();
            }
        } else {
            dialogHelper.showErrorDialog("Ошибка", "Неверный логин или пароль");
            passwordField.clear();
        }
    }

    /**
     * Обрабатывает событие регистрации пользователя.
     * Проверяет введенные логин и пароль, выполняет регистрацию
     * и показывает соответствующие сообщения об успехе или ошибке.
     *
     * @param event событие нажатия кнопки регистрации
     */
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