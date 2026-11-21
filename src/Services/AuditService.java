package Services;

import Models.AuditRecord;
import Models.Purchase;
import Models.Product;
import Models.User;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AuditService {
    private static final Gson gson = new GsonBuilder()
            .create();

    private static final DailyJsonListService<AuditRecord> storage =
            new DailyJsonListService<>(
                    JsonStorage.getAuditDirectory(),
                    AuditRecord.class,
                    record -> LocalDateTime
                            .parse(record.getTimestamp())
                            .toLocalDate()
            );

    // REGISTRAR UN EVENTO
    public static void record(String action,
            String entityType, String entityId,
            String beforeData, String afterData) {

        if (!Session.isLoggedIn()) {
            throw new IllegalStateException(
                    "No hay un usuario conectado para registrar la auditoría."
            );
        }

        AuditRecord record = new AuditRecord(
                UUID.randomUUID().toString(),
                Session.getCurrentUserId(),
                Session.getCurrentUsername(),
                Session.isAdmin() ? "ADMIN" : "VENDEDOR",
                action,
                entityType,
                entityId,
                beforeData,
                afterData
        );

        storage.add(record);
    }

    // CONVERTIR UN USUARIO A DATOS SEGUROS
    private static String userSnapshot(User user) {
        if (user == null) {
            return null;
        }

        JsonObject data = new JsonObject();

        data.addProperty("id", user.getId());
        data.addProperty("username", user.getUsername());
        data.addProperty("admin", user.isAdmin());

        return data.toString();
    }

    // REGISTRAR CAMBIOS DE USUARIOS
    public static void recordUserChange(
            String action, User before, User after) {
        User reference = after != null ? after : before;

        if (reference == null) {
            throw new IllegalArgumentException(
                    "No se proporcionó un usuario."
            );
        }

        record(
                action,
                "USUARIO",
                reference.getId(),
                userSnapshot(before),
                userSnapshot(after)
        );
    }

    // REGISTRAR CAMBIOS DE VENTAS
    public static void recordPurchaseChange(
            String action, Purchase before, Purchase after) {
        Purchase reference = after != null ? after : before;

        if (reference == null) {
            throw new IllegalArgumentException(
                    "No se proporcionó una venta."
            );
        }

        record(
                action, "VENTA",
                reference.getId(),
                before == null ? null : gson.toJson(before),
                after == null ? null : gson.toJson(after)
        );
    }

    // CONSULTAR AUDITORÍA DE UN DÍA
    public static List<AuditRecord> getByDate(LocalDate date) {
        return storage.readByDate(date);
    }

    // CONSULTAR AUDITORÍA ENTRE DOS FECHAS
    public static List<AuditRecord> getBetween(
            LocalDate start, LocalDate end) {
        return storage.readBetween(start, end);
    }

    public static void recordProductChange(
            String action, Product before, Product after) {
        Product reference = after != null ? after : before;

        if (reference == null) {
            throw new IllegalArgumentException(
                    "No se proporcionó un producto."
            );
        }

        record(
                action,
                "PRODUCTO",
                reference.getId(),
                before == null ? null : gson.toJson(before),
                after == null ? null : gson.toJson(after)
        );
    }
}
