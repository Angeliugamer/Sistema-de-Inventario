package Controllers;

import Components.ProductListCell;
import Models.Product;

import Services.CartSession;
import Services.CalculadoraVenta;
import Services.PurchaseService;
import Services.Session;
import Services.VentaService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VentaController {
    @FXML private Button Historial;
    @FXML private Button Continuar;
    @FXML private Button Eliminar;

    @FXML private ListView<Product> ListaVenta;

    @FXML private Label TotalLabel;
    @FXML private Label CantidadLabel;

    @FXML private TextField Cantidad;

    private final CalculadoraVenta calculadoraVenta = new CalculadoraVenta();

    private final Map<String, Integer> carrito = new HashMap<>(CartSession.getItems());
    private final Map<String, Product> productos = new HashMap<>();

    @FXML
    public void initialize() {
        boolean admin = Session.isAdmin();

        Historial.setVisible(admin);
        Historial.setManaged(admin);

        configurarLista();
        cargarProductos();
        actualizarResumen();
    }

    // CONFIGURAR LA LISTA DE PRODUCTOS DE LA VENTA.
    private void configurarLista() {
        ListaVenta.setCellFactory(list ->
                new ProductListCell(
                        ProductListCell.Mode.INVENTORY,
                        carrito
                )
        );
    }

    // CARGAR LOS PRODUCTOS DEL CARRITO.
    private void cargarProductos() {
        try {
            productos.clear();

            for (Product producto : Services.ProductService.getProducts()) {
                productos.put(producto.getId(), producto);
            }

            actualizarLista();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("No se pudieron cargar los productos.");
        }
    }

    private void actualizarLista() {
        List<Product> seleccionados = new ArrayList<>();

        carrito.entrySet().removeIf(entrada -> {
            Product producto = productos.get(entrada.getKey());

            return producto == null || entrada.getValue() <= 0;
        });

        for (String id : carrito.keySet()) {
            Product producto = productos.get(id);

            if (producto != null) {
                seleccionados.add(producto);
            }
        }

        ListaVenta.setItems(FXCollections.observableArrayList(seleccionados));

        ListaVenta.refresh();
        actualizarResumen();
    }

    // ACTUALIZAR LA CANTIDAD DEL PRODUCTO SELECCIONADO.
    @FXML
    private void actualizarCantidad() {
        Product seleccionado = ListaVenta.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAviso("Selecciona un producto.");
            return;
        }

        try {
            int cantidad = Integer.parseInt(Cantidad.getText().trim());

            if (cantidad <= 0) {
                mostrarAviso("La cantidad debe ser mayor que cero.");
                return;
            }

            if (cantidad > seleccionado.getStock()) {
                mostrarAviso(
                        "Stock insuficiente. Disponible: "
                                + seleccionado.getStock()
                );
                return;
            }

            carrito.put(seleccionado.getId(), cantidad);
            CartSession.setQuantity(seleccionado.getId(), cantidad);

            Cantidad.clear();
            actualizarLista();

        } catch (NumberFormatException e) {
            mostrarAviso("Introduce una cantidad entera válida.");
        }
    }

    // ELIMINAR UN PRODUCTO DE LA VENTA.
    @FXML
    private void eliminarProducto() {
        Product seleccionado = ListaVenta.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAviso("Selecciona un producto.");
            return;
        }

        carrito.remove(seleccionado.getId());
        CartSession.setQuantity(seleccionado.getId(), 0);

        actualizarLista();
    }

    // CALCULAR EL TOTAL Y LA CANTIDAD DE UNIDADES.
    private void actualizarResumen() {
        double total = 0;
        int unidades = 0;

        for (Map.Entry<String, Integer> entrada : carrito.entrySet()) {
            Product producto = productos.get(entrada.getKey());

            if (producto == null) {
                continue;
            }

            int cantidad = entrada.getValue();

            total += calculadoraVenta.calcular(producto.getPrice(), cantidad);

            unidades += cantidad;
        }

        TotalLabel.setText(String.format(
                Locale.forLanguageTag("es-CO"),
                "Total: $%,.0f",
                total
        ));

        CantidadLabel.setText("Unidades: " + unidades);
    }

    // CONFIRMAR Y REGISTRAR LA VENTA.
    @FXML
    public void purchase() {
        if (carrito.isEmpty()) {
            mostrarAviso("No hay productos en la venta.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);

        confirmacion.setTitle("Confirmar venta");
        confirmacion.setHeaderText("¿Deseas finalizar esta venta?");
        confirmacion.setContentText(TotalLabel.getText());

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL)
                != ButtonType.OK) {
            return;
        }

        try {
            double total = VentaService.registrarVenta(Map.copyOf(carrito));

            CartSession.clear();
            carrito.clear();

            cargarProductos();

            mostrarInformacion(String.format(
                    Locale.forLanguageTag("es-CO"),
                    "Venta registrada correctamente.\nTotal: $%,.0f",
                    total
            ));

        } catch (Exception e) {
            e.printStackTrace();

            mostrarError(e.getMessage() == null
                    ? "No se pudo registrar la venta."
                    : e.getMessage()
            );
        }
    }

    @FXML
    private void openHistory() {
        if (!Session.isAdmin()) {
            return;
        }

        try {
            AppNavigator.showHistoryFromVenta();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("No se pudo abrir el historial.");
        }
    }

    @FXML
    private void logout() {
        CartSession.clear();
        Session.logout();

        try {
            AppNavigator.showLogin();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("No se pudo cerrar la sesión.");
        }
    }

    @FXML
    private void backToProducts() {
        try {
            AppNavigator.showInventory();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("No se pudo regresar al inventario.");
        }
    }

    private void mostrarAviso(String mensaje) {
        new Alert(Alert.AlertType.WARNING, mensaje).showAndWait();
    }

    private void mostrarError(String mensaje) {
        new Alert(Alert.AlertType.ERROR, mensaje).showAndWait();
    }

    private void mostrarInformacion(String mensaje) {
        new Alert(Alert.AlertType.INFORMATION, mensaje).showAndWait();
    }
}
