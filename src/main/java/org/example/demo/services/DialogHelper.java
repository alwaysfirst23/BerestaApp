package org.example.demo.services;

import javafx.scene.control.Alert;

public class DialogHelper {

    /**
     * Отображение диалогового окна с сообщением об ошибке.
     *
     * @param title заголовок диалогового окна
     * @param message сообщение об ошибке, которое будет отображено в диалоговом окне
     */
    public void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Отображение диалогового окна с информационным сообщением.
     *
     * @param title заголовок диалогового окна
     * @param message информационное сообщение, которое будет отображено в диалоговом окне
     */
    public void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}