package org.example.demo;

import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Класс для тестировки, сюда можно писать всё что хочется :)
 */

public class Test {
    public static void main(String[] args) {
        DatabaseInitializer.authInitialize();
        DatabaseInitializer.taskInitialize();
    }
}
