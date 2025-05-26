package org.example.demo.presentation.auth;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

public class AuthDialog {

    private AuthController controller;
    private Dialog<Boolean> dialog;
    private DialogHelper dialogHelper;

//    public AuthDialog(DialogHelper dialogHelper) throws IOException {
//        this.dialogHelper = dialogHelper;
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth_view.fxml"));
//        Parent root = loader.load();
//        controller = new AuthController(dialogHelper);
//
//        dialog = new Dialog<>();
//        dialog.getDialogPane().setContent(root);
//
//        ButtonType loginButtonType = new ButtonType("Войти", ButtonBar.ButtonData.OK_DONE);
//        ButtonType registerButtonType = new ButtonType("Регистрация", ButtonBar.ButtonData.APPLY);
//        ButtonType cancelButtonType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
//
//        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, registerButtonType, cancelButtonType);
//
//        //  ... (Остальная логика AuthDialog, например, обработка кнопок)
//    }
//
//    public void setRegisterMode(boolean isRegisterMode) {
//        controller.setRegisterMode(isRegisterMode);
//        dialog.setTitle(controller.getTitle());
//        dialog.setHeaderText(controller.getHeaderText());
//
//        // Получаем кнопку "Войти" или "Зарегистрироваться"
//        Button okButton = (Button) dialog.getDialogPane().lookupButton(
//                dialog.getDialogPane().getButtonTypes().stream()
//                        .filter(buttonType -> buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE || buttonType.getButtonData() == ButtonBar.ButtonData.APPLY)
//                        .findFirst()
//                        .orElse(null)
//        );
//
//        if (okButton != null) {
//            if (isRegisterMode) {
//                okButton.setText("Зарегистрироваться");
//            } else {
//                okButton.setText("Войти");
//            }
//        }
//    }
//
//    public void setValidationCallback(Consumer<Boolean> callback) {
//        controller.setValidationCallback(callback);
//    }
//
//    public Optional<Boolean> showAndWait() {
//        return dialog.showAndWait();
//    }
//
//    public void focusLoginField() {
//        controller.focusLoginField();
//    }
//
//    public String getLogin() {
//        return controller.getLogin();
//    }
//
//    public String getPassword() {
//        return controller.getPassword();
//    }
}