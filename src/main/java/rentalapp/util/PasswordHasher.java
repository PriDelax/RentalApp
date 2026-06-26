package rentalapp.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHasher {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public static String hash(String password) {
        return encoder.encode(password);
    }

    public static boolean verify(String password, String hashedPassword) {
        try {
            return encoder.matches(password, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Если хеш в БД повреждён или не в формате BCrypt
            System.err.println("Ошибка проверки пароля: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        String pass = "test123";
        String hash = hash(pass);
        System.out.println("Пароль: " + pass);
        System.out.println("Хеш: " + hash);
        System.out.println("Длина хеша: " + hash.length());
        System.out.println("Проверка (верный): " + verify(pass, hash));
        System.out.println("Проверка (неверный): " + verify("wrong", hash));
    }
}