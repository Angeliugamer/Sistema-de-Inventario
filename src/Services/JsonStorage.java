package Services;

import Models.User;
import Models.Purchase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class JsonStorage {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Path DATA_DIR = Path.of(System.getProperty("user.dir"), "data");
    private static final Path ADMINS_DIR = DATA_DIR.resolve("Admins");
    private static final Path SELLERS_DIR = DATA_DIR.resolve("Vendedor");
    private static final Path ADMIN_USERS_FILE = ADMINS_DIR.resolve("users.json");
    private static final Path SELLER_USERS_FILE = SELLERS_DIR.resolve("users.json");
    private static final Path PURCHASES_FILE = DATA_DIR.resolve("purchases.json");
    private static final Path PRODUCTS_FILE = DATA_DIR.resolve("products.json");
    private static final Path AUDIT_FILE = DATA_DIR.resolve("audit.json");

    private static final Type USER_LIST =
            new TypeToken<ArrayList<User>>() {}.getType();

    private static final Type PURCHASE_LIST =
            new TypeToken<ArrayList<Purchase>>() {}.getType();

    static {
        try {
            Files.createDirectories(ADMINS_DIR);
            Files.createDirectories(SELLERS_DIR);

            createFile(ADMIN_USERS_FILE);
            createFile(SELLER_USERS_FILE);
            createFile(PURCHASES_FILE);
            createFile(PRODUCTS_FILE);
            createFile(AUDIT_FILE);

        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static void createFile(Path path) throws IOException {
        if (Files.notExists(path)) {
            Files.writeString(
                    path, "[]",
                    StandardCharsets.UTF_8
            );
        }
    }

    public static synchronized <T> List<T> read(Path path, Type type) {
        try {
            createFile(path);

            String json = Files.readString(
                    path, StandardCharsets.UTF_8);

            List<T> result = gson.fromJson(json, type);

            return result != null ? result : new ArrayList<>();

        } catch (IOException e) {
            throw new RuntimeException(
                    "Error leyendo JSON: " + path, e);
        }
    }

    public static synchronized <T> void write(Path path, List<T> data) {
        try {
            Files.createDirectories(path.getParent());

            Path temporaryFile = path.resolveSibling(path.getFileName() + ".tmp");

            Files.writeString(
                    temporaryFile,
                    gson.toJson(data),
                    StandardCharsets.UTF_8
            );

            try {
                Files.move(temporaryFile, path,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );

            } catch (IOException atomicError) {
                Files.move(temporaryFile, path,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

        } catch (IOException e) {
            throw new RuntimeException(
                    "Error guardando JSON: " + path, e
            );
        }
    }

    // USUARIOS ADMINISTRADORES
    public static synchronized List<User> readAdmins() {return read(ADMIN_USERS_FILE, USER_LIST);}
    public static synchronized void saveAdmins(List<User> users) {write(ADMIN_USERS_FILE, users);}

    // USUARIOS VENDEDORES
    public static synchronized List<User> readSellers() {return read(SELLER_USERS_FILE, USER_LIST);}
    public static synchronized void saveSellers(List<User> users) {write(SELLER_USERS_FILE, users);}

    // COMPRAS Y VENTAS (se conserva la estructura anterior)
    public static synchronized List<Purchase> readPurchases() {return read(PURCHASES_FILE, PURCHASE_LIST);}
    public static synchronized void savePurchases(List<Purchase> purchases) {write(PURCHASES_FILE, purchases);}

    // RUTAS DE LOS ARCHIVOS
    public static Path getDataDirectory() {return DATA_DIR;}
    public static Path getAdminsFile() {return ADMIN_USERS_FILE;}
    public static Path getSellersFile() {return SELLER_USERS_FILE;}

    // RUTAS DE LAS LISTAS
    public static Path getProductsFile() {return PRODUCTS_FILE;}
    public static Path getPurchasesFile() {return PURCHASES_FILE;}
    public static Path getAuditFile() {return AUDIT_FILE;}
    public static Path getAdminUsersFile() {return ADMIN_USERS_FILE;}
    public static Path getSellerUsersFile() {return SELLER_USERS_FILE;}
}
