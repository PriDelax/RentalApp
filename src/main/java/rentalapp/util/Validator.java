package rentalapp.util;

import java.util.regex.Pattern;

public class Validator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{10,15}$");

    public static boolean isValidLogin(String login) {
        if (login == null || login.trim().isEmpty()) return false;
        return login.trim().length() >= 3 && login.trim().length() <= 50;
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.replaceAll("\\s", "")).matches();
    }

    public static boolean isValidFullName(String fullName) {
        return fullName != null && fullName.trim().length() >= 2 && fullName.trim().length() <= 100;
    }

    public static boolean isValidPassport(String passport) {
        return passport != null && passport.trim().length() >= 4;
    }

    public static boolean doPasswordsMatch(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }

    public static boolean isValidPaymentDetails(String details) {
        return details != null && details.trim().length() >= 5;
    }
}