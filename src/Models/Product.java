package Models;

import Interfaces.Gestionable;
import Interfaces.Identificable;

public class Product implements Identificable, Gestionable {
    private String id;
    private String name;
    private String category;
    private double price;
    private int stock;
    private int stockMinimo = 5;
    private boolean active = true;

    // 1. Constructor vacío (obligatorio para que Gson lea el JSON)
    public Product() {}

    public Product(String id, String name, String category, double price, int stock) {
        this(id, name, category, price, stock, 5);
    }

    // 2. Constructor completo con validaciones
    public Product(String id, String name, String category, double price, int stock, int stockMinimo) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El ID del producto no puede estar vacío.");
        }
        this.id = id;
        setName(name);
        setCategory(category);
        setPrice(price);
        setStock(stock);
        setStockMinimo(stockMinimo);
    }

    // --- MÉTODOS DE LA INTERFAZ Identificable ---
    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }

        this.name = name.trim();
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category == null || category.isBlank()
                ? "General"
                : category.trim();
    }

    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        if (!Double.isFinite(price) || price < 0) {
            throw new IllegalArgumentException("El precio debe ser un número válido y no negativo.");
        }

        this.price = price;
    }

    public int getStock() {
        return stock;
    }
    public void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }

        this.stock = stock;
    }

    public int getStockMinimo() {return stockMinimo;}
    public void setStockMinimo(int stockMinimo) {
        if (stockMinimo < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo.");
        }

        this.stockMinimo = stockMinimo;
    }

    // --- REGLAS DE NEGOCIO DEL PRODUCTO ---
    public boolean tieneStockSuficiente(int cantidad) {
        return cantidad > 0 && this.stock >= cantidad;
    }

    public void descontarStock(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a descontar debe ser mayor a 0.");
        }

        if (!tieneStockSuficiente(cantidad)) {
            throw new IllegalStateException("Stock insuficiente para el producto: " + this.name);
        }

        this.stock -= cantidad;
    }

    public void reponerStock(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a reponer debe ser mayor a 0.");
        }

        stock = Math.addExact(stock, cantidad);
    }

    public double calcularSubtotal(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0.");
        }

        return price * cantidad;
    }

    // --- MÉTODOS DE LA INTERFAZ Gestionable ---
    @Override
    public boolean hayStock() {
        return this.stock > 0;
    }

    @Override
    public String getResumen() {
        return String.format("%s [%s] - $%.2f (Stock: %d / Mínimo: %d)", name, category, price, stock, stockMinimo);
    }

    // --- REPRESENTACIÓN EN TEXTO PARA JAVAFX (ListView / ComboBox) ---
    @Override
    public String toString() {
        return getResumen();
    }

    public boolean isActive() {return active;}
    public void setActive(boolean active) {this.active = active;}
}
