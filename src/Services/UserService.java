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
        byte[] decodedSalt = Base64.getDecoder().decode(salt);

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

    private static boolean usernameExists(String username) {
        String normalized = username.trim();

        boolean existsInAdmins = JsonStorage.readAdmins()
                .stream()
                .anyMatch(user ->
                        user.getUsername().equalsIgnoreCase(normalized)
                );

        boolean existsInSellers = JsonStorage.readSellers()
                .stream()
                .anyMatch(user ->
                        user.getUsername().equalsIgnoreCase(normalized)
                );

        return existsInAdmins || existsInSellers;
    }

    private static User createUser(
            String username, String password,
            boolean admin) throws Exception {
        String salt = generateSalt();
        String hash = hashPassword(password, salt);

        return new User(
                UUID.randomUUID().toString(),
                username.trim(),
                hash,
                salt,
                admin
        );
    }

    // REGISTRO NORMAL: SIEMPRE CREA VENDEDORES
    public static synchronized boolean registerSeller(
            String username, String password) {
        try {
            // Solo un administrador autenticado puede registrar vendedores
            if (!Session.isAdmin()) {
                return false;
            }

            if (username == null || password == null || username.isBlank() || password.isBlank()) {
                return false;
            }

            if (usernameExists(username)) {
                return false;
            }

            User newUser = createUser(
                    username,
                    password,
                    false
            );

            List<User> sellers = JsonStorage.readSellers();

            sellers.add(newUser);
            JsonStorage.saveSellers(sellers);

            AuditService.recordUserChange(
                    "CREAR", null, newUser
            );

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // CREACIÓN DE ADMINISTRADORES EXCLUSIVAMENTE POR CÓDIGO
    public static synchronized boolean createAdminByCode(
            String username, String password) {
        try {
            if (username == null || password == null || username.isBlank() || password.isBlank()) {
                return false;
            }

            if (usernameExists(username)) {
                return false;
            }

            User newAdmin = createUser(
                    username,
                    password,
                    true
            );

            List<User> admins = JsonStorage.readAdmins();
            admins.add(newAdmin);
            JsonStorage.saveAdmins(admins);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // AUTENTICACIÓN
    public static synchronized User authenticate(
            String username, String password) {
        if (username == null || password == null || username.isBlank() || password.isEmpty()) {
            return null;
        }

        try {
            String normalized = username.trim();

            List<User> admins = JsonStorage.readAdmins();
            List<User> sellers = JsonStorage.readSellers();

            User adminMatch = findUser(admins, normalized);
            User sellerMatch = findUser(sellers, normalized);

            // Un usuario no debe existir en ambos archivos
            if (adminMatch != null && sellerMatch != null) {
                System.err.println(
                        "Inconsistencia: usuario duplicado en Admins y Vendedor."
                );
                return null;
            }

            User user = adminMatch != null
                    ? adminMatch
                    : sellerMatch;

            if (user == null) {
                return null;
            }

            String attemptedHash = hashPassword(
                    password, user.getPasswordSalt()
            );

            byte[] actual = Base64.getDecoder().decode(
                    user.getPasswordHash()
            );

            byte[] attempted = Base64.getDecoder().decode(
                    attemptedHash
            );

            if (!MessageDigest.isEqual(actual, attempted)) {
                return null;
            }

            // El atributo admin determina el rol real,
            // no la carpeta donde se encontró el usuario.
            if (user.isAdmin() && sellerMatch != null) {
                moveUser(user, sellers, admins);

            } else if (!user.isAdmin() && adminMatch != null) {
                moveUser(user, admins, sellers);
            }

            return user;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static User findUser(
            List<User> users, String username) {
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }

        return null;
    }

    // CORREGIR LA UBICACIÓN DEL USUARIO
    private static void moveUser(
            User user,
            List<User> source,
            List<User> destination) {
        source.removeIf(existing ->
                existing.getId().equals(user.getId())
        );

        destination.add(user);

        // Guardar primero el destino.
        // Si falla la segunda escritura, el próximo inicio
        // detectará el duplicado y rechazará el acceso.
        if (user.isAdmin()) {
            JsonStorage.saveAdmins(destination);
            JsonStorage.saveSellers(source);
        } else {
            JsonStorage.saveSellers(destination);
            JsonStorage.saveAdmins(source);
        }
    }
}
