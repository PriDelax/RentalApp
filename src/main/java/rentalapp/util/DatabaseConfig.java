package rentalapp.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {
    private static final Properties props = new Properties();
    static {
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("Файл config.properties не найден!");
            }
            props.load(input);
            System.out.println("Конфигурация БД загружена");
        } catch (IOException e) {
            System.err.println("Ошибка чтения config.properties: " + e.getMessage());
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public static String getUrl() {
        String host = get("db.host", "localhost");
        String port = get("db.port", "5432");
        String dbName = get("db.name", "postgres");
        return "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
    }

    public static String getUser() {
        return get("db.username", "postgres");
    }

    public static String getPassword() {
        return get("db.password", "");
    }
}