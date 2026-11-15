package Services;

import Models.User;

public class Session {
    private static User currentUser;
    public static void login(User user) {
        currentUser = user;
    }
    public static User getCurrentUser() {return currentUser;}

    public static String getCurrentUserId() {
        return currentUser != null
                ? currentUser.getId()
                : null;
    }

    public static String getCurrentUsername() {
        return currentUser != null
                ? currentUser.getUsername()
                : null;
    }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public static boolean isLoggedIn() {return currentUser != null;}
    public static void logout() {currentUser = null;}
}
