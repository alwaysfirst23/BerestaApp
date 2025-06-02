package org.example.demo.presentation.auth;

import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import lombok.Setter;

import java.util.function.Consumer;

/**
 * Отвечает за построение UI компонентов диалога
 */
public class AuthView {
    private TextField loginField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    @Setter
    private Consumer<Boolean> validationCallback;
    private boolean isRegisterMode;

    public AuthView() {
        initializeFields();
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

    /**
     * Создает и возвращает корневой элемент (GridPane) для UI диалогового окна.
     */
    public Parent build() {
        GridPane grid = createGrid();
        addFieldsToGrid(grid);
        setupValidation();
        return grid;
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

    /**
     * Инициализирует поля ввода для логина, пароля и подтверждения пароля.
     */
    private void initializeFields() {
        loginField = new TextField();
        passwordField = new PasswordField();
        confirmPasswordField = new PasswordField();

        loginField.setPromptText("Логин");
        passwordField.setPromptText("Пароль");
        confirmPasswordField.setPromptText("Подтвердите пароль");
    }

    /**
     * Создает и настраивает сетку для расположения UI компонентов.
     *
     * @return созданная сетка GridPane
     */
    private GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setMaxWidth(Double.MAX_VALUE);
        return grid;
    }

    /**
     * Добавляет поля ввода и подписи к сетке.
     *
     * @param grid сетка, к которой будут добавлены поля
     */
    private void addFieldsToGrid(GridPane grid) {
        grid.addRow(0, new Label("Логин:"), loginField);
        grid.addRow(1, new Label("Пароль:"), passwordField);

        if (isRegisterMode) {
            grid.addRow(2, new Label("Подтверждение:"), confirmPasswordField);
        }

        GridPane.setHgrow(loginField, Priority.ALWAYS);
        GridPane.setHgrow(passwordField, Priority.ALWAYS);
    }

    /**
     * Устанавливает валидацию полей ввода.
     */
    private void setupValidation() {
        Consumer<String> validator = s -> validateFields();
        loginField.textProperty().addListener((obs, old, newVal) -> validator.accept(newVal));
        passwordField.textProperty().addListener((obs, old, newVal) -> validator.accept(newVal));

        if (isRegisterMode) {
            confirmPasswordField.textProperty().addListener((obs, old, newVal) -> validator.accept(newVal));
        }
    }

    /**
     * Проверяет валидность заполненных полей ввода.
     */
    private void validateFields() {
        boolean isValid = !getLogin().isEmpty()
                && !getPassword().isEmpty()
                && (!isRegisterMode || getPassword().equals(confirmPasswordField.getText()));

        if (validationCallback != null) {
            validationCallback.accept(isValid);
        }
    }
}