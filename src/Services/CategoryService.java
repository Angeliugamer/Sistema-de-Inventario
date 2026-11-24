package Services;

import Models.Product;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Administra el catálogo de categorías en data/categories.json.
 * También incorpora automáticamente las categorías que ya existen
 * en los productos guardados, para no perder datos anteriores.
 */
public final class CategoryService {
    private static final Path FILE = Path.of(
            System.getProperty("user.dir"), "data", "categories.json"
    );

    private static final Type CATEGORY_LIST =
            new TypeToken<ArrayList<String>>() {}.getType();

    private CategoryService() {
    }

    public static synchronized List<String> getCategories() {
        List<String> saved = JsonStorage.read(FILE, CATEGORY_LIST);
        Map<String, String> unique = new LinkedHashMap<>();

        for (String category : saved) {
            addUnique(unique, category);
        }

        // Migración compatible: recupera categorías de productos existentes.
        for (Product product : ProductService.getAllProducts()) {
            addUnique(unique, product.getCategory());
        }

        List<String> result = new ArrayList<>(unique.values());

        // Persiste las categorías antiguas en el nuevo catálogo.
        if (!result.equals(saved)) {
            JsonStorage.write(FILE, result);
        }

        return List.copyOf(result);
    }

    public static synchronized String resolveCategory(String category) {
        String key = normalizeKey(category);
        if (key.isEmpty()) {
            return null;
        }

        return getCategories().stream()
                .filter(existing -> normalizeKey(existing).equals(key))
                .findFirst()
                .orElse(null);
    }

    public static synchronized String addCategory(String category) {
        String normalized = normalizeDisplay(category);

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre de la categoría no puede estar vacío."
            );
        }

        List<String> categories = new ArrayList<>(getCategories());
        String key = normalizeKey(normalized);

        boolean duplicate = categories.stream()
                .anyMatch(existing -> normalizeKey(existing).equals(key));

        if (duplicate) {
            throw new IllegalArgumentException(
                    "Esa categoría ya existe. No se permiten duplicados por mayúsculas o espacios."
            );
        }

        categories.add(normalized);
        JsonStorage.write(FILE, categories);
        return normalized;
    }

    private static void addUnique(Map<String, String> categories, String value) {
        String display = normalizeDisplay(value);
        if (!display.isEmpty()) {
            categories.putIfAbsent(normalizeKey(display), display);
        }
    }

    private static String normalizeDisplay(String value) {
        if (value == null) {
            return "";
        }

        return value.trim().replaceAll("\\s+", " ");
    }

    /** Quita espacios y normaliza mayúsculas solo para comparar. */
    private static String normalizeKey(String value) {
        return normalizeDisplay(value)
                .replaceAll("\\s+", "")
                .toLowerCase(Locale.ROOT);
    }
}
