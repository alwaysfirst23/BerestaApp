package org.example.demo;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.util.Optional;

public class AuthDialog {
    private final AuthService authService;
    private boolean isRegisterMode = false;

    public AuthDialog(AuthService authService) {
        this.authService = authService;
    }

    public void setRegisterMode(boolean registerMode) {
        this.isRegisterMode = registerMode;
    }

    public Optional<Pair<String, String>> showAndWait() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle(isRegisterMode ? "Регистрация" : "Авторизация");
        dialog.setHeaderText(isRegisterMode ? "Создайте новый аккаунт" : "Введите логин и пароль");

        // Элементы формы
        TextField loginField = new TextField();
        PasswordField passwordField = new PasswordField();
        PasswordField confirmField = isRegisterMode ? new PasswordField() : null;

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        grid.add(new Label("Логин:"), 0, 0);
        grid.add(loginField, 1, 0);
        grid.add(new Label("Пароль:"), 0, 1);
        grid.add(passwordField, 1, 1);

        if (isRegisterMode) {
            grid.add(new Label("Подтвердите пароль:"), 0, 2);
            grid.add(confirmField, 1, 2);
        }

        dialog.getDialogPane().setContent(grid);

        // Кнопки
        ButtonType actionButtonType = new ButtonType(
                isRegisterMode ? "Зарегистрироваться" : "Войти",
                ButtonBar.ButtonData.OK_DONE
        );
        dialog.getDialogPane().getButtonTypes().addAll(actionButtonType, ButtonType.CANCEL);

        // Валидация
        Node actionButton = dialog.getDialogPane().lookupButton(actionButtonType);
        actionButton.setDisable(true);

        Runnable validate = () -> {
            boolean valid = !loginField.getText().trim().isEmpty()
                    && !passwordField.getText().trim().isEmpty();

            if (isRegisterMode) {
                valid = valid
                        && !confirmField.getText().trim().isEmpty()
                        && passwordField.getText().equals(confirmField.getText());
            }

            actionButton.setDisable(!valid);
        };

        loginField.textProperty().addListener((obs, old, newVal) -> validate.run());
        passwordField.textProperty().addListener((obs, old, newVal) -> validate.run());
        if (isRegisterMode) {
            confirmField.textProperty().addListener((obs, old, newVal) -> validate.run());
        }

        // Фокусировка
        Platform.runLater(() -> loginField.requestFocus());

        // Результат
        dialog.setResultConverter(buttonType -> {
            if (buttonType == actionButtonType) {
                return new Pair<>(loginField.getText(), passwordField.getText());
            }
            return null;
        });

        return dialog.showAndWait();
    }
}
