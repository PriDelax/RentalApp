package rentalapp.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Session {
    private int userId;
    private String userName;
    private String role;
    @Getter
    @Setter
    private static Session currentSession;

    public static boolean isLoggedIn() {
        return currentSession != null;
    }

    public static void logout() {
        currentSession = null;
    }

    public static String getCurrentUserRole() {
        return currentSession != null ? currentSession.getRole() : null;
    }
}