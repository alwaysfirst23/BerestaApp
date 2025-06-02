package org.example.demo.services;

import org.example.demo.domain.User;
import org.example.demo.infrastructure.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;

/**
 * Реализует логику взаимодействия с базой данных - регистрацию и авторизацию
 */
public class AuthService {
    private final UserRepository userRepository = new UserRepository();

    /**
     * Реализует логику регистрации пользователя
     * @param username login, юзернейм
     * @param password пароль
     * @return true, если пользователь успешко зарегистрирован; false, если пользователь с таким логином ужн существует
     */
    public boolean register(String username, String password) {
        if (userRepository.findUserByUsername(username) != null) {
            System.out.println("Пользователь с таким именем уже существует.");
            return false;
        }
        User user = new User(username, password);
        userRepository.registerUser(user);
        return true;
    }

    /**
     * Реализует логику авторизации пользователя
     * @param username login, юзернейм
     * @param password пароль
     * @return Объект User если авторизация успешна, null если неправильные данные
     */
    public User login(String username, String password) {
        User user = userRepository.findUserByUsername(username);
        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    /**
     * Получает полную информацию о пользователе
     * @param username Логин пользователя
     * @return Объект User или null если не найден
     */
    public User getUserProfile(String username) {
        return userRepository.findUserByUsername(username);
    }

    /**
     * Обновляет аватар пользователя
     * @param username Логин пользователя
     * @param avatarPath URL или путь к аватару
     * @return true если обновление успешно
     */

    public boolean updateAvatar(String username, String avatarPath) {
        if (username == null || username.isBlank()) {
            return false;
        }

        if (avatarPath == null || avatarPath.isBlank()) {
            return false;
        }

        // Проверяем расширение файла
        String extension = avatarPath.substring(avatarPath.lastIndexOf(".") + 1).toLowerCase();
        if (!extension.matches("png|jpg|jpeg|gif")) {
            return false;
        }

        return userRepository.updateUserField(username, "avatar_url", avatarPath);
    }

    private boolean isValidImageUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        // Проверяем локальные файлы
        if (url.startsWith("file:")) {
            return true;
        }

        // Проверяем URL изображений
        return url.matches("(?i).*\\.(png|jpg|jpeg|gif|svg|webp)(\\?.*)?$");
    }

    /**
     * Обновляет отображаемое имя пользователя с валидацией
     */
    public boolean updateDisplayName(String username, String displayName) {
        if (username == null || username.isBlank()) {
            return false;
        }

        if (displayName == null || displayName.isBlank()) {
            return false;
        }

        if (displayName.length() > 50) {
            return false;
        }

        return userRepository.updateUserField(username, "display_name", displayName);
    }
}
