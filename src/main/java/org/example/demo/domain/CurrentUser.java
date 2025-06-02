package org.example.demo.domain;

import lombok.Getter;
import lombok.Setter;

public class CurrentUser {
    @Getter
    @Setter
    private static User instance;

    public static String getUsername() {
        return instance != null ? instance.getUsername() : "Гость";
    }

    public static String getDisplayName() {
        return instance != null && instance.getDisplayName() != null ?
                instance.getDisplayName() : getUsername();
    }

    public static String getAvatarUrl() {
        return instance != null ? instance.getAvatarUrl() : "/profile.png";
    }
}
