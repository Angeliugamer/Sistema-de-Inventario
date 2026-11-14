package Models;

import java.time.LocalDateTime;

public class Purchase {
    private String id;
    private String userId;
    private String product;
    private int quantity;
    private double total;
    private String date;

    public Purchase(String id, String userId, String product, int quantity, double total) {
        this.id = id;
        this.userId = userId;
        this.product = product;
        this.quantity = quantity;
        this.total = total;
        this.date = LocalDateTime.now().toString();
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotal() {
        return total;
    }

    public String getDate() {
        return date;
    }
}
