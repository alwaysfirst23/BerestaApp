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
     * Реализует логику авторизациипользователя
     * @param username login, юзернейм
     * @param password пароль
     * @return true, если авторизация успешна, false, если неправильные данные
     */
    public boolean login(String username, String password) {
        User user = userRepository.findUserByUsername(username);
        if (user != null) {
            return BCrypt.checkpw(password, user.getPassword()); // Проверка пароля
        }
        return false;
    }
}
