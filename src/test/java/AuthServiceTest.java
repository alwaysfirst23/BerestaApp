import org.example.demo.domain.User;
import org.example.demo.services.AuthService;
import org.example.demo.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class AuthServiceTest {

    private AuthService authService;
    private UserRepository userRepository;
    private final String TEST_USER = "testUser";
    private final String TEST_PASS = "testPass123";

    @BeforeEach
    void setUp() {
        // Инициализируем UserRepository и AuthService перед каждым тестом
        userRepository = new UserRepository(); // Замените на вашу реализацию UserRepository
        authService = new AuthService();
    }

    @Test
    void testUpdateDisplayName() {
        // Регистрируем тестового пользователя
        authService.register(TEST_USER, TEST_PASS);

        String displayName = "Тестовый Пользователь";

        // Обновляем имя
        boolean updateResult = authService.updateDisplayName(TEST_USER, displayName);
        assertTrue(updateResult, "Обновление имени должно быть успешным");

        // Проверяем, что имя сохранилось
        User user = authService.getUserProfile(TEST_USER);
        assertEquals(displayName, user.getDisplayName(), "Имя пользователя должно обновиться");
    }


    @Test
    void testUpdateAvatar() {
        authService.register(TEST_USER, TEST_PASS);

        String avatarUrl = "avatars/Света.jpg";
        boolean updateResult = authService.updateAvatar(TEST_USER, avatarUrl);

        assertTrue(updateResult, "Обновление аватара должно быть успешным");

        User user = authService.getUserProfile(TEST_USER);
        assertEquals(avatarUrl, user.getAvatarUrl(), "URL аватара должен обновиться");
    }

    @Test
    void testRegistrationAndLogin() {
        String testLogin = "testUser";
        String testPassword = "testPassword";

        // Регистрация пользователя
        boolean registrationResult = authService.register(testLogin, testPassword);
        assertTrue(registrationResult, "Регистрация должна быть успешной");

        // Авторизация пользователя
        boolean loginResult = authService.login(testLogin, testPassword);
        assertTrue(loginResult, "Авторизация должна быть успешной");
    }

    @Test
    void testLoginWithIncorrectPassword() {
        String testLogin = "testUser";
        String incorrectPassword = "incorrectPassword";

        // Попытка авторизации с неверным паролем
        boolean loginResult = authService.login(testLogin, incorrectPassword);
        assertFalse(loginResult, "Авторизация с неверным паролем должна завершиться неудачей");
    }

    @Test
    void testRegistrationWithExistingUser() {
        String testLogin = "existingUser";
        String testPassword = "password";

        // Регистрируем пользователя
        authService.register(testLogin, testPassword);

        // Попытка регистрации пользователя с тем же логином
        boolean registrationResult = authService.register(testLogin, "newPassword");
        assertFalse(registrationResult, "Регистрация с существующим логином должна завершиться неудачей");
    }

    @Test
    void testLoginWithNonexistentUser() {
        String nonExistentLogin = "nonExistentUser";
        String testPassword = "password";

        // Попытка авторизации несуществующего пользователя
        boolean loginResult = authService.login(nonExistentLogin, testPassword);
        assertFalse(loginResult, "Авторизация несуществующего пользователя должна завершиться неудачей");
    }
}