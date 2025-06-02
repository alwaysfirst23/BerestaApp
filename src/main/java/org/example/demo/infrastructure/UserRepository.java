package org.example.demo.infrastructure;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.example.demo.domain.User;
import org.mindrot.jbcrypt.BCrypt;

/**
 *   Класс работает с базой данных
 */

public class UserRepository {

    /**
     *  Регистрирует пользователя, добавляя в базу новую запись
     * @param user объект класса User, содержащий данные пользователя
     */

    public void registerUser(User user) {
        String sql = "INSERT INTO users(username, password) VALUES(?, ?)";
        // Генерируем хэш пароля с автоматической солью
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());

        try (Connection conn = DatabaseConnector.authConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, hashedPassword);
            pstmt.executeUpdate();
            System.out.println("Пользователь зарегистрирован.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Обновляет поле пользователя в базе данных
     * @param username Логин пользователя
     * @param fieldName Название поля (display_name или avatar_url)
     * @param value Новое значение
     * @return true если обновление успешно
     */
    public boolean updateUserField(String username, String fieldName, String value) {
        String sql = "UPDATE users SET " + fieldName + " = ? WHERE username = ?";

        try (Connection conn = DatabaseConnector.authConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, value);
            pstmt.setString(2, username);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении поля " + fieldName + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Находит пользователя по логину
     * @param username Логин пользователя
     * @return Объект User со всеми полями или null если не найден
     */
    public User findUserByUsername(String username) {
        String sql = "SELECT username, password, display_name, avatar_url FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnector.authConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("display_name"),
                        rs.getString("avatar_url")
                );
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске пользователя: " + e.getMessage());
        }
        return null;
    }
}
