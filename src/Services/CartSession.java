package Services;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CartSession {
    private static final Map<String, Integer> items = new HashMap<>();

    public static void setQuantity(String productId, int quantity) {
        if (quantity <= 0) {
            items.remove(productId);
        } else {
            items.put(productId, quantity);
        }
    }

    public static int getQuantity(String productId) {
        return items.getOrDefault(productId, 0);
    }

    public static Map<String, Integer> getItems() {
        return Collections.unmodifiableMap(new HashMap<>(items));
    }

    public static boolean isEmpty() {
        return items.isEmpty();
    }

    public static void clear() {
        items.clear();
    }
}