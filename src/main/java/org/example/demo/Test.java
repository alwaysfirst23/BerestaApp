package org.example.demo;

import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Класс для тестировки, сюда можно писать всё что хочется :)
 */

public class Test {
    public static void main(String[] args) {
        AuthService authService = new AuthService();
        String login = "login";
        String password = "password";
        authService.register(login, password);

        authService.login(login, password);
    }
}
