package Models;

import Interfaces.Gestionable;
import Interfaces.Identificable;

public class Product implements Identificable, Gestionable {
    private String id;
    private String name;
    private String category;
    private double price;
    private int stock;
    private boolean active = true;

    // 1. Constructor vacío (obligatorio para que Gson lea el JSON)
    public Product() {
    }

    // 2. Constructor completo con validaciones
    public Product(String id, String name, String category, double price, int stock) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El ID del producto no puede estar vacío.");
        }
        this.id = id;
        setName(name);
        setCategory(category);
        setPrice(price);
        setStock(stock);
    }

    // --- MÉTODOS DE LA INTERFAZ Identificable ---
    @Override
    public String getId() {
        return id;
    }

    // --- GETTERS Y SETTERS CON REGLAS DE VALIDACIÓN ---
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
        if (category == null || category.isBlank()) {
            this.category = "General";
        } else {
            this.category = category.trim();
        }
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
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
        this.stock += cantidad;
    }

    public double calcularSubtotal(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0.");
        }
        return this.price * cantidad; // Corregido: operador de multiplicación añadido
    }

    // --- MÉTODOS DE LA INTERFAZ Gestionable ---
    @Override
    public boolean hayStock() {
        return this.stock > 0;
    }

    @Override
    public String getResumen() {
        return String.format("%s [%s] - $%.2f (Stock: %d)", name, category, price, stock);
    }

    // --- REPRESENTACIÓN EN TEXTO PARA JAVAFX (ListView / ComboBox) ---
    @Override
    public String toString() {
        return getResumen();
    }

    public boolean isActive() {return active;}
    public void setActive(boolean active) {this.active = active;}
}
