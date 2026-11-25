package Services;

import Models.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductService {
    private static final JsonListService<Product> storage =
            new JsonListService<>(
                    JsonStorage.getProductsFile(),
                    Product.class
            );

    // CONSULTAR PRODUCTOS ACTIVOS
    public static List<Product> getProducts() {
        return storage.readAll()
                .stream()
                .filter(Product::isActive)
                .toList();
    }

    // Obtener todos los productos del inventario.
    public static List<Product> getAllProducts() {
        return storage.readAll();
    }

    // BUSCAR PRODUCTOS
    public static List<Product> searchProducts(String search) {
        if (search == null || search.isBlank()) {
            return getProducts();
        }

        String text = search.trim().toLowerCase();

        return getProducts()
                .stream()
                .filter(p ->
                        p.getName().toLowerCase(java.util.Locale.ROOT).contains(text)
                                || p.getCategory().toLowerCase(java.util.Locale.ROOT).contains(text)
                                || p.getId().toLowerCase(java.util.Locale.ROOT).contains(text)
                )
                .toList();
    }

    // AGREGAR PRODUCTO
    public static void addProduct(
            String name, String category,
            double price, int stock
    ) {
        addProduct(name, category, price, stock, 5);
    }

    public static void addProduct(
            String name, String category,
            double price, int stock, int stockMinimo) {
        requireAdmin();

        Product product = new Product.Builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .category(category)
                .price(price)
                .stock(stock)
                .stockMinimo(stockMinimo)
                .build();

        storage.add(product);

        AuditService.recordProductChange(
                "CREAR", null, product
        );
    }

    // MODIFICAR PRODUCTO
    public static void updateProduct(Product updated) {
        requireAdmin();

        if (updated == null || updated.getId() == null) {
            throw new IllegalArgumentException("Producto no válido.");
        }

        List<Product> products = new ArrayList<>(storage.readAll());

        Product previous = products.stream()
                .filter(p -> p.getId().equals(updated.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "El producto no existe."
                ));

        Product before = copy(previous);
        updated.setActive(previous.isActive());

        products.removeIf(p -> p.getId().equals(updated.getId()));
        products.add(updated);

        JsonStorage.write(JsonStorage.getProductsFile(), products);
        AuditService.recordProductChange("MODIFICAR", before, updated);
    }

    // REABASTECER: aumenta el stock sin crear ni reemplazar el producto.
    public static void replenishStock(String productId, int quantity) {
        requireAdmin();

        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Selecciona un producto válido.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad a reponer debe ser mayor que cero."
            );
        }

        List<Product> products = new ArrayList<>(storage.readAll());
        Product product = products.stream()
                .filter(p -> p.getId().equals(productId) && p.isActive())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "El producto no existe o está desactivado."
                ));

        Product before = copy(product);
        product.reponerStock(quantity);

        JsonStorage.write(JsonStorage.getProductsFile(), products);
        AuditService.recordProductChange("REABASTECER", before, product);
    }

    // ELIMINACIÓN LÓGICA
    public static void deactivateProduct(String id) {
        requireAdmin();

        List<Product> products = new ArrayList<>(storage.readAll());

        Product product = products.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "El producto no existe."
                ));

        if (!product.isActive()) {
            throw new IllegalStateException("El producto ya está desactivado.");
        }

        Product before = copy(product);
        product.setActive(false);

        JsonStorage.write(JsonStorage.getProductsFile(), products);
        AuditService.recordProductChange("ELIMINAR", before, product);
    }

    private static Product copy(Product product) {
        return new Product.Builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .stock(product.getStock())
                .stockMinimo(product.getStockMinimo())
                .active(product.isActive())
                .build();
    }

    // COMPROBAR PERMISOS
    private static void requireAdmin() {
        if (!Session.isAdmin()) {
            throw new SecurityException(
                    "Esta operación requiere permisos de administrador."
            );
        }
    }
}
