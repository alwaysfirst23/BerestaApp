package org.example.demo.presentation.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import lombok.Setter;
import org.example.demo.services.AuthService;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

public class AuthController {

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private GridPane gridPane; // Ссылка на корневой элемент GridPane

    @Setter
    private Consumer<Boolean> validationCallback;

    private boolean isRegisterMode;

    private DialogHelper dialogHelper;
    private AuthService authService = new AuthService(); // Добавляем поле для AuthService
    // Добавляем метод setStage
    @Setter
    private Stage stage; // Добавляем поле для Stage

    public AuthController() { // Конструктор по умолчанию
        this.dialogHelper = new DialogHelper(); // Инициализируем DialogHelper
    }

    public AuthController(DialogHelper dialogHelper) { // Конструктор с аргументом
        this.dialogHelper = dialogHelper;
    }

    @FXML
    public void initialize() {
        setupValidation();
    }

    public void setRegisterMode(boolean isRegisterMode) {
        this.isRegisterMode = isRegisterMode;
    }

    public String getTitle() {
        return isRegisterMode ? "Регистрация" : "Авторизация";
    }

    public String getHeaderText() {
        return isRegisterMode ? "Создание нового аккаунта" : "Введите учетные данные";
    }

    public void focusLoginField() {
        loginField.requestFocus();
    }

    public String getLogin() {
        return loginField.getText().trim();
    }

    public String getPassword() {
        return passwordField.getText().trim();
    }

    private void setupValidation() {
        Consumer<String> validator = s -> validateFields();
        loginField.textProperty().addListener((obs, old, newVal) -> validator.accept(newVal));
        passwordField.textProperty().addListener((obs, old, newVal) -> validator.accept(newVal));
    }

    private void validateFields() {
        boolean isValid = !getLogin().isEmpty()
                && !getPassword().isEmpty();

        if (validationCallback != null) {
            validationCallback.accept(isValid);
        }
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String login = getLogin();
        String password = getPassword();

        boolean loginResult = authService.login(login, password);

        if (loginResult) {
            dialogHelper.showSuccessDialog("Успешная авторизация");
            try {
                Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/main_window.fxml")));
                Stage mainWindowStage = new Stage();
                mainWindowStage.setScene(new Scene(root, 800, 600)); // Устанавливаем размеры
                mainWindowStage.setTitle("Beresta"); // Устанавливаем заголовок
                mainWindowStage.show();
                stage.close(); // Закрываем окно авторизации
            } catch (IOException e) {
                e.printStackTrace();
                dialogHelper.showErrorDialog("Ошибка приложения", "Не удалось загрузить основное окно");
            }
        } else {
            dialogHelper.showErrorDialog("Ошибка авторизации", "Неверный логин или пароль");
        }
    }

    @FXML
    public void handleRegistration() {
        String login = getLogin();
        String password = getPassword();

        boolean registrationResult = authService.register(login, password);

        if (registrationResult) {
            dialogHelper.showSuccessDialog("Успешная регистрация");
            // Добавьте логику перехода к следующему окну
        } else {
            dialogHelper.showErrorDialog("Ошибка регистрации", "Пользователь с таким логином уже существует");
        }
    }
}