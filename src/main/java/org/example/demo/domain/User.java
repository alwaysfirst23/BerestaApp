package org.example.demo.domain;

import lombok.Getter;
import lombok.Setter;

/**
 *  Класс пользователя, содержит логин, пароль, id
 */

@Getter
@Setter
public class User {
    private int id;
    private String username;
    private String password;
    private String displayName;
    private String avatarUrl;

    public User(String username, String password) {
        this(username, password, null, null);
    }

    public User(String username, String password, String displayName, String avatarUrl) {
        this.username = username;
        this.password = password;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
    }
}
