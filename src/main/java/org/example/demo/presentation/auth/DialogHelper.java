package org.example.demo.presentation.auth;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class DialogHelper {
    public Optional<ButtonType> showConfirmationDialog(String title, String header, ButtonType... buttons) {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.getButtonTypes().setAll(buttons);
        return dialog.showAndWait();
    }

    /**
     * Вызывает диалоговое окно с сообщением об успешном действии
     * @param message сообщение
     */
    public void showSuccessDialog(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Успех", message);
    }

    /**
     * Вызывает диалоговое окно с сообщением об ошибке авторизации/регистрации
     * @param title заголовок
     * @param message текст ошибки
     */
    public void showErrorDialog(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    /**
     * Создаёт диалоговое окно
     * @param type тип (успешно / ошибка)
     * @param title заголовок
     * @param content текст
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
