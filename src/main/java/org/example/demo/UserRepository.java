package org.example.demo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

        try (Connection conn = DatabaseConnector.connect();
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
     * Ищет пользователя в базе по логину
     * @param username login пользователя
     * @return объект класса User; null - если пользователь не найден
     */
    public User findUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        User user = null;

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                user = new User(rs.getString("username"), rs.getString("password"));
                user.setId(rs.getInt("id"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return user;
    }
}
