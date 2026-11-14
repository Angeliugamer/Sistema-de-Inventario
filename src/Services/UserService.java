package Services;

import Models.User;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class UserService {
    private static final int ITERATIONS = 210000;
    private static final int KEY_LENGTH = 256;

    private static String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        return Base64.getEncoder().encodeToString(salt);
    }

    private static String hashPassword(
            String password, String salt) throws Exception {

        byte[] decodedSalt =
                Base64.getDecoder().decode(salt);

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                decodedSalt,
                ITERATIONS,
                KEY_LENGTH
        );

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256");

            byte[] hash = factory.generateSecret(spec).getEncoded();

            return Base64.getEncoder().encodeToString(hash);

        } finally {
            spec.clearPassword();
        }
    }

    public static boolean register(
            String username, String password) {

        try {
            if (username == null || password == null ||
                    username.isBlank() || password.isBlank()) {
                return false;
            }

            List<User> users = JsonStorage.readUsers();

            boolean exists = users.stream().anyMatch(
                    user -> user.getUsername().equalsIgnoreCase(
                            username.trim()));

            if (exists) {
                return false;
            }

            String salt = generateSalt();

            String hash = hashPassword(password, salt);

            User newUser = new User(
                    UUID.randomUUID().toString(),
                    username.trim(),
                    hash,
                    salt
            );

            users.add(newUser);

            JsonStorage.saveUsers(users);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static User authenticate(
            String username, String password) {

        if (username == null || password == null) {
            return null;
        }

        try {
            List<User> users = JsonStorage.readUsers();

            for (User user : users) {

                if (user.getUsername().equalsIgnoreCase(
                        username.trim())) {

                    String attemptedHash = hashPassword(
                            password,
                            user.getPasswordSalt()
                    );

                    byte[] actual = Base64.getDecoder().decode(
                            user.getPasswordHash());

                    byte[] attempted = Base64.getDecoder().decode(
                            attemptedHash);

                    if (MessageDigest.isEqual(actual, attempted)) {
                        return user;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
