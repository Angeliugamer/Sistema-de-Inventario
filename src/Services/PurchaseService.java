package Services;

import Models.Purchase;

import java.util.List;
import java.util.UUID;

public class PurchaseService {
    public static void addPurchase(
            String product, int quantity, double total) {

        String userId = Session.getCurrentUserId();

        if (userId == null) {
            throw new IllegalStateException(
                    "No hay ningún usuario conectado.");
        }

        if (product == null || product.isBlank()
                || quantity <= 0 || total < 0) {
            throw new IllegalArgumentException(
                    "Los datos de la compra no son válidos.");
        }

        List<Purchase> purchases =
                JsonStorage.readPurchases();

        Purchase purchase = new Purchase(
                UUID.randomUUID().toString(),
                userId,
                product,
                quantity,
                total
        );

        purchases.add(purchase);

        JsonStorage.savePurchases(purchases);
    }

    public static List<Purchase> getUserPurchases() {

        String userId = Session.getCurrentUserId();

        if (userId == null) {
            throw new IllegalStateException(
                    "No hay ningún usuario conectado.");
        }

        return JsonStorage.readPurchases()
                .stream()
                .filter(p -> p.getUserId().equals(userId))
                .toList();
    }
}
