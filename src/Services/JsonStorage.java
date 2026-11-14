package Services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JsonStorage {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Path DATA_DIR =
            Path.of(System.getProperty("user.home"),
                    ".sistema-inventario");

    private static final Path USERS_FILE =
            DATA_DIR.resolve("users.json");

    private static final Path PURCHASES_FILE =
            DATA_DIR.resolve("purchases.json");

    private static final Type USER_LIST =
            new TypeToken<ArrayList<Models.User>>() {}.getType();

    private static final Type PURCHASE_LIST =
            new TypeToken<ArrayList<Models.Purchase>>() {}.getType();

    static {
        try {
            Files.createDirectories(DATA_DIR);

            createFile(USERS_FILE);
            createFile(PURCHASES_FILE);

        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static void createFile(Path path) throws IOException {
        if (Files.notExists(path)) {
            Files.writeString(path, "[]", StandardCharsets.UTF_8);
        }
    }

    public static synchronized <T> List<T> read(
            Path path, Type type) {

        try {
            String json = Files.readString(
                    path, StandardCharsets.UTF_8);

            List<T> result = gson.fromJson(json, type);

            return result != null ? result : new ArrayList<>();

        } catch (IOException e) {
            throw new RuntimeException(
                    "Error leyendo JSON: " + path, e);
        }
    }

    public static synchronized <T> void write(
            Path path, List<T> data) {

        try {
            String json = gson.toJson(data);

            Files.writeString(
                    path,
                    json,
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {
            throw new RuntimeException(
                    "Error guardando JSON: " + path, e);
        }
    }

    public static List<Models.User> readUsers() {
        return read(USERS_FILE, USER_LIST);
    }

    public static void saveUsers(List<Models.User> users) {
        write(USERS_FILE, users);
    }

    public static List<Models.Purchase> readPurchases() {
        return read(PURCHASES_FILE, PURCHASE_LIST);
    }

    public static void savePurchases(
            List<Models.Purchase> purchases) {

        write(PURCHASES_FILE, purchases);
    }
}
