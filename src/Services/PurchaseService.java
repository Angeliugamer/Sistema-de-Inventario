package Services;

import Models.Purchase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PurchaseService {
    private static final DailyJsonListService<Purchase> storage =
            new DailyJsonListService<>(
                    JsonStorage.getPurchasesDirectory(),
                    Purchase.class,
                    purchase -> LocalDateTime
                            .parse(purchase.getDate())
                            .toLocalDate()
            );

    // REGISTRAR UNA VENTA
    public static void addPurchase(
            String product, int quantity, double total) {
        String userId = Session.getCurrentUserId();

        if (userId == null) {
            throw new IllegalStateException(
                    "No hay ningún usuario conectado."
            );
        }

        if (product == null || product.isBlank() || quantity <= 0 || total < 0) {
            throw new IllegalArgumentException(
                    "Los datos de la compra no son válidos."
            );
        }

        Purchase purchase = new Purchase(
                UUID.randomUUID().toString(),
                userId,
                product,
                quantity,
                total
        );

        storage.add(purchase);

        // Guardar el registro de auditoría
        AuditService.recordPurchaseChange(
                "CREAR",
                null,
                purchase
        );
    }

    // REGISTRAR VARIOS PRODUCTOS DE UNA MISMA VENTA.
    public static void addPurchases(List<Purchase> purchases) {
        if (!Session.isLoggedIn()) {
            throw new IllegalStateException("No hay ningún usuario conectado.");
        }

        if (purchases == null || purchases.isEmpty()) {
            throw new IllegalArgumentException("La venta no contiene productos.");
        }

        for (Purchase purchase : purchases) {
            if (purchase == null || !Session.getCurrentUserId().equals(purchase.getUserId())) {
                throw new IllegalArgumentException("Los datos de la venta no son válidos.");
            }
        }

        storage.addAll(purchases);

        for (Purchase purchase : purchases) {
            AuditService.recordPurchaseChange("CREAR", null, purchase);
        }
    }

    // CONSULTAR LAS COMPRAS DEL USUARIO ACTUAL
    public static List<Purchase> getUserPurchases() {
        String userId = Session.getCurrentUserId();

        if (userId == null) {
            throw new IllegalStateException(
                    "No hay ningún usuario conectado."
            );
        }

        return storage.readAll()
                .stream()
                .filter(p -> p.getUserId().equals(userId))
                .toList();
    }

    // CONSULTAR LAS VENTAS DE UN DÍA
    public static List<Purchase> getPurchasesByDate(
            LocalDate date) {
        if (!Session.isAdmin()) {
            throw new SecurityException(
                    "Solo un administrador puede consultar el historial."
            );
        }

        return storage.readByDate(date);
    }

    // CONSULTAR LAS VENTAS ENTRE FECHAS
    public static List<Purchase> getPurchasesBetween(
            LocalDate start, LocalDate end) {
        if (!Session.isAdmin()) {
            throw new SecurityException(
                    "Solo un administrador puede consultar el historial."
            );
        }

        return storage.readBetween(start, end);
    }
}
