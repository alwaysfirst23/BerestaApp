package org.example.demo.presentation.main;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import org.example.demo.domain.CurrentUser;
import org.example.demo.services.AuthService;
import org.example.demo.services.DialogHelper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ProfileTabController {
    @FXML private ImageView avatarImageView;
    @FXML private Label usernameLabel;
    @FXML private Label userInfoLabel;
    @FXML private Button changeAvatarBtn;
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
        String avatarPath = CurrentUser.getAvatarUrl();

        usernameLabel.setText(displayName != null ? displayName : username);

        try {
            if (avatarPath != null && !avatarPath.isEmpty()) {
                File avatarFile = new File(avatarPath);
                if (avatarFile.exists()) {
                    Image avatarImage = new Image(avatarFile.toURI().toString());
                    avatarImageView.setImage(avatarImage);
                } else {
                    loadDefaultAvatar();
                }
            } else {
                loadDefaultAvatar();
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки аватара: " + e.getMessage());
            loadDefaultAvatar();
        }

        userInfoLabel.setText("Добро пожаловать, " + (displayName != null ? displayName : username) + "!");
    }

    private void loadDefaultAvatar() {
        avatarImageView.setImage(new Image(getClass().getResourceAsStream("/profile.png")));
    }

    @FXML
    private void handleChangeAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите изображение для аватара");

        // Устанавливаем фильтры для изображений
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
                "Изображения", "*.png", "*.jpg", "*.jpeg", "*.gif");
        fileChooser.getExtensionFilters().add(imageFilter);

        File selectedFile = fileChooser.showOpenDialog(avatarImageView.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Создаем папку avatars в домашней директории пользователя
                String userHome = System.getProperty("user.home");
                Path avatarsDir = Path.of(userHome, ".beresta", "avatars");
                Files.createDirectories(avatarsDir);

                // Формируем имя файла: username_avatar.ext
                String extension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."));
                String avatarFileName = CurrentUser.getUsername() + "_avatar" + extension;
                Path destPath = avatarsDir.resolve(avatarFileName);

                // Копируем файл
                Files.copy(selectedFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);

                // Загружаем новое изображение
                Image newAvatar = new Image(destPath.toUri().toString());
                avatarImageView.setImage(newAvatar);

                // Сохраняем путь к файлу в базе данных
                boolean success = authService.updateAvatar(
                        CurrentUser.getUsername(),
                        destPath.toString()
                );

                if (success) {
                    CurrentUser.getInstance().setAvatarUrl(destPath.toString());
                    dialogHelper.showInfoDialog("Успех", "Аватар успешно изменен");
                } else {
                    dialogHelper.showErrorDialog("Ошибка", "Не удалось сохранить аватар в базе данных");
                }
            } catch (Exception e) {
                dialogHelper.showErrorDialog("Ошибка", "Не удалось загрузить изображение: " + e.getMessage());
                e.printStackTrace();
            }
        }
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