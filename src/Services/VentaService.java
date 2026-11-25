package Services;

import Models.Product;
import Models.Purchase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VentaService {
    private static final JsonListService<Product> productStorage =
            new JsonListService<>(
                    JsonStorage.getProductsFile(),
                    Product.class
            );

    private static final CalculadoraVenta calculadora = new CalculadoraVenta();

    // REGISTRAR UNA VENTA COMPLETA.
    public static synchronized double registrarVenta(
            Map<String, Integer> carrito) {
        if (!Session.isLoggedIn()) {
            throw new IllegalStateException("Debes iniciar sesión para registrar una venta.");
        }

        if (carrito == null || carrito.isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío.");
        }

        List<Product> originales = new ArrayList<>(productStorage.readAll());
        List<Product> actualizados = new ArrayList<>();

        Map<String, Product> productosPorId = new HashMap<>();

        // Crear copias para no modificar los objetos originales.
        for (Product original : originales) {
            Product copia = copiarProducto(original);
            actualizados.add(copia);
            productosPorId.put(copia.getId(), copia);
        }

        List<Purchase> ventas = new ArrayList<>();
        double totalVenta = 0;

        // Validar todos los productos antes de guardar.
        for (Map.Entry<String, Integer> entrada : carrito.entrySet()) {
            String id = entrada.getKey();
            int cantidad = entrada.getValue();

            if (cantidad <= 0) {
                throw new IllegalArgumentException("La cantidad de un producto no es válida.");
            }

            Product producto = productosPorId.get(id);

            if (producto == null || !producto.isActive()) {
                throw new IllegalStateException("Uno de los productos ya no está disponible.");
            }

            if (!producto.tieneStockSuficiente(cantidad)) {
                throw new IllegalStateException(
                        "Stock insuficiente para " + producto.getName()
                                + ". Disponible: " + producto.getStock()
                );
            }

            double subtotal = calculadora.calcular(producto.getPrice(), cantidad);

            totalVenta += subtotal;

            ventas.add(new Purchase(
                    UUID.randomUUID().toString(),
                    Session.getCurrentUserId(),
                    producto.getName(),
                    cantidad,
                    subtotal
            ));

            producto.descontarStock(cantidad);
        }

        // Guardar primero el inventario actualizado.
        JsonStorage.write(JsonStorage.getProductsFile(), actualizados);

        try {
            // Registrar todas las líneas de venta juntas.
            PurchaseService.addPurchases(ventas);

        } catch (Exception e) {
            // Si falla el registro de ventas, restaurar el inventario.
            JsonStorage.write(JsonStorage.getProductsFile(), originales);

            throw new RuntimeException(
                    "No se pudo registrar la venta. "
                            + "El inventario fue restaurado.", e
            );
        }

        return totalVenta;
    }

    private static Product copiarProducto(Product producto) {
        return new Product.Builder()
                .id(producto.getId())
                .name(producto.getName())
                .category(producto.getCategory())
                .price(producto.getPrice())
                .stock(producto.getStock())
                .stockMinimo(producto.getStockMinimo())
                .active(producto.isActive())
                .build();
    }
}
