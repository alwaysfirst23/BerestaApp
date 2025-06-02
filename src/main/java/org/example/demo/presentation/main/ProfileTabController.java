package org.example.demo.presentation.main;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.demo.domain.CurrentUser;

public class ProfileTabController {
    @FXML private ImageView avatarImageView;
    @FXML private Label usernameLabel;
    @FXML private Label userInfoLabel;

    @FXML
    public void initialize() {
        loadUserData();
    }

    private void loadUserData() {
        // Получаем данные текущего пользователя
        String username = CurrentUser.getUsername();
        String displayName = CurrentUser.getDisplayName();
        String avatarUrl = CurrentUser.getAvatarUrl();

        // Устанавливаем имя пользователя
        usernameLabel.setText(displayName != null ? displayName : username);

        // Загружаем аватар
        try {
            Image avatarImage;
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                if (avatarUrl.startsWith("/")) {
                    // Локальный ресурс
                    avatarImage = new Image(avatarUrl);
                } else {
                    // Внешний URL
                    avatarImage = new Image(avatarUrl);
                }
            } else {
                // Аватар по умолчанию
                avatarImage = new Image(getClass().getResourceAsStream("/profile.png"));
            }
            avatarImageView.setImage(avatarImage);
        } catch (Exception e) {
            System.err.println("Ошибка загрузки аватара: " + e.getMessage());
            avatarImageView.setImage(new Image(getClass().getResourceAsStream("/profile.png")));
        }

        // Дополнительная информация
        userInfoLabel.setText("Добро пожаловать в ваш профиль!\n"
                + "Логин: " + username + "\n"
                + "Статус: Активен");
    }
}
