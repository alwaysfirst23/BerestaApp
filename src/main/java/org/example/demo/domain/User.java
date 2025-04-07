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

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
