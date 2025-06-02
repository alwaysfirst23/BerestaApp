package org.example.demo.services;

import org.example.demo.domain.User;
import org.example.demo.infrastructure.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

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
     * Обновляет аватар пользователя
     * @param username Логин пользователя
     * @param avatarUrl URL или путь к аватару
     * @return true если обновление успешно
     */
    public boolean updateAvatar(String username, String avatarUrl) {
        if (username == null || username.isBlank()) {
            return false;
        }

        // Опционально: валидация URL/пути
        if (!isValidAvatarUrl(avatarUrl)) {
            return false;
        }

        return userRepository.updateUserField(username, "avatar_url", avatarUrl);
    }

    /**
     * Получает полную информацию о пользователе
     * @param username Логин пользователя
     * @return Объект User или null если не найден
     */
    public User getUserProfile(String username) {
        return userRepository.findUserByUsername(username);
    }

    // Простая валидация URL аватара
    private boolean isValidAvatarUrl(String url) {
        if (url == null || url.isBlank()) {
            return true; // Разрешаем null/пустую строку
        }
        // Проверяем допустимые расширения файлов
        return url.matches("(?i).*\\.(png|jpg|jpeg|gif|svg|webp)$");
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
