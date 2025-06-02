package org.example.demo.presentation.main;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import org.example.demo.domain.CurrentUser;
import org.example.demo.services.AuthService;
import org.example.demo.services.DialogHelper;

public class ProfileTabController {
    @FXML private ImageView avatarImageView;
    @FXML private Label usernameLabel;
    @FXML private Label userInfoLabel;
    @FXML private GridPane editForm;
    @FXML private TextField nameField;

    private final AuthService authService = new AuthService();
    private final DialogHelper dialogHelper = new DialogHelper();

    @FXML
    public void initialize() {
        loadUserData();
    }

    private void loadUserData() {
        String username = CurrentUser.getUsername();
        String displayName = CurrentUser.getDisplayName();
        String avatarUrl = CurrentUser.getAvatarUrl();

        usernameLabel.setText(displayName != null ? displayName : username);

        try {
            Image avatarImage = new Image(
                    String.valueOf(avatarUrl != null && !avatarUrl.isEmpty() ?
                            avatarUrl : getClass().getResourceAsStream("/profile.png"))
            );
            avatarImageView.setImage(avatarImage);
        } catch (Exception e) {
            System.err.println("Ошибка загрузки аватара: " + e.getMessage());
            avatarImageView.setImage(new Image(getClass().getResourceAsStream("/profile.png")));
        }

        userInfoLabel.setText("Добро пожаловать в ваш профиль!\n" +
                "Логин: " + username + "\n" +
                "Статус: Активен");
    }

    @FXML
    private void handleEditName() {
        nameField.setText(CurrentUser.getDisplayName());
        editForm.setVisible(true);
    }

    @FXML
    private void handleSaveName() {
        String newName = nameField.getText().trim();

        if (newName.isEmpty()) {
            dialogHelper.showErrorDialog("Ошибка", "Имя не может быть пустым");
            return;
        }

        boolean success = authService.updateDisplayName(
                CurrentUser.getUsername(),
                newName
        );

        if (success) {
            CurrentUser.getInstance().setDisplayName(newName);
            usernameLabel.setText(newName);
            editForm.setVisible(false);
            dialogHelper.showInfoDialog("Успех", "Имя успешно изменено");
        } else {
            dialogHelper.showErrorDialog("Ошибка", "Не удалось изменить имя");
        }
    }

    @FXML
    private void handleCancelEdit() {
        editForm.setVisible(false);
    }
}
