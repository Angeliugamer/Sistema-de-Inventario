package Controllers;

import Models.Product;
import Services.ProductService;
import Services.Session;
import Services.ButtonSound;
import Services.StockAlertManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;

import javafx.geometry.Insets;

import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ListProductController {
    @FXML private Button Out;
    @FXML private Button Shop;
    @FXML private Button Regist;
    @FXML private Button Historial;
    @FXML private Button Reabastecer;

    @FXML private ListView<Product> ListProd;
    @FXML private TextField BarraBusqueda;
    @FXML private TextField Cantidad;
    @FXML private Button BtnBusqueda;
    @FXML private Button BtnAñadir;
    @FXML private Button BtnEliminar;

    private FilteredList<Product> productosFiltrados;

    // ID del producto -> cantidad temporal en el carrito.
    private final Map<String, Integer> carrito = new HashMap<>();
    private static final PseudoClass IN_CART = PseudoClass.getPseudoClass("in-cart");
    private final StockAlertManager stockAlertManager = new StockAlertManager();

    @FXML
    public void initialize() {
        boolean admin = Session.isAdmin();

        Regist.setVisible(admin);
        Regist.setManaged(admin);

        Historial.setVisible(admin);
        Historial.setManaged(admin);

        Reabastecer.setVisible(admin);
        Reabastecer.setManaged(admin);

        configurarLista();
        configurarBusqueda();

        cargarProductos();

        ListProd.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, anterior, actual) -> {
                    Cantidad.setPromptText(actual == null
                            ? "Selecciona un producto"
                            : "Cantidad (1 por defecto)"
                    );
                });

        Platform.runLater(() -> {
            if (ListProd.getScene() != null && ListProd.getScene().getRoot() instanceof StackPane root) {
                stockAlertManager.start(root);
            }
        });
    }

    // CONFIGURAR LA LISTA Y SUS FILAS.
    private void configurarLista() {
        ListProd.setCellFactory(list -> new ListCell<>() {
            private final GridPane fila = new GridPane();

            private final Label nombre = new Label();
            private final Label categoria = new Label();
            private final Label precio = new Label();
            private final Label stock = new Label();
            private final Label cantidad = new Label();

            {
                fila.setHgap(8);
                fila.setVgap(0);

                double[] widths = {32, 20, 18, 15, 15};
                for (double width : widths) {
                    ColumnConstraints column = new ColumnConstraints();
                    column.setPercentWidth(width);
                    column.setHgrow(Priority.ALWAYS);
                    fila.getColumnConstraints().add(column);
                }

                configurarLabel(nombre);
                configurarLabel(categoria);
                configurarLabel(precio);
                configurarLabel(stock);
                configurarLabel(cantidad);

                fila.add(nombre, 0, 0);
                fila.add(categoria, 1, 0);
                fila.add(precio, 2, 0);
                fila.add(stock, 3, 0);
                fila.add(cantidad, 4, 0);

                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

                // El gris de hover tiene prioridad sobre el estado del stock.
                setOnMouseEntered(event -> {
                    if (!isEmpty()) {
                        aplicarFondo(Color.web("#D3D3D3"));
                    }
                });
                setOnMouseExited(event -> actualizarFondo(getItem()));
            }

            @Override
            protected void updateItem(Product producto, boolean empty) {
                super.updateItem(producto, empty);

                if (empty || producto == null) {
                    setGraphic(null);
                    setText(null);
                    setBackground(Background.EMPTY);
                    pseudoClassStateChanged(IN_CART, false);
                    return;
                }

                nombre.setText(producto.getName());
                categoria.setText(producto.getCategory());
                precio.setText(
                        String.format(
                                Locale.forLanguageTag("es-CO"),
                                "$%,.0f",
                                producto.getPrice()
                        )
                );

                stock.setText(String.valueOf(producto.getStock()));

                int units = carrito.getOrDefault(producto.getId(), 0);

                cantidad.setText(
                        units > 0
                                ? String.valueOf(units)
                                : ""
                );

                pseudoClassStateChanged(IN_CART, units > 0);

                setGraphic(fila);
                actualizarFondo(producto);
            }

            private void configurarLabel(Label label) {
                label.setMaxWidth(Double.MAX_VALUE);
                label.setTextAlignment(TextAlignment.LEFT);
                label.setStyle("-fx-padding: 5 2 5 2; -fx-text-fill: black;");
            }

            private void actualizarFondo(Product producto) {
                if (producto == null) {
                    setBackground(Background.EMPTY);
                    return;
                }

                if (isHover()) {
                    aplicarFondo(Color.web("#D3D3D3"));
                } else if (producto.getStock() == 0) {
                    aplicarFondo(Color.web("#F4CCCC"));
                } else if (producto.getStock() <= producto.getStockMinimo()) {
                    aplicarFondo(Color.web("#FFF2CC"));
                } else {
                    aplicarFondo(Color.WHITE);
                }
            }

            private void aplicarFondo(Color color) {
                setBackground(new Background(new BackgroundFill(
                        color, CornerRadii.EMPTY, Insets.EMPTY
                )));
            }
        });
    }

    // CARGAR LOS PRODUCTOS DESDE JSON.
    private void cargarProductos() {
        try {
            List<Product> productos = ProductService.getProducts();

            productosFiltrados = new FilteredList<>(
                    FXCollections.observableArrayList(productos),
                    producto -> true
            );

            ListProd.setItems(productosFiltrados);
            ListProd.refresh();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("No se pudieron cargar los productos.");
        }
    }

    // CONFIGURAR LA BÚSQUEDA EN TIEMPO REAL.
    private void configurarBusqueda() {
        BarraBusqueda.textProperty().addListener(
                (observable, anterior, actual) ->
                        aplicarBusqueda(actual)
        );
    }

    @FXML
    private void buscarProductos() {
        aplicarBusqueda(BarraBusqueda.getText());
    }

    private void aplicarBusqueda(String texto) {
        if (productosFiltrados == null) {
            return;
        }

        String busqueda = texto == null
                ? ""
                : texto.trim().toLowerCase(Locale.ROOT);

        productosFiltrados.setPredicate(producto -> busqueda.isEmpty()
                || producto.getName().toLowerCase(Locale.ROOT).contains(busqueda)
                || producto.getCategory().toLowerCase(Locale.ROOT).contains(busqueda)
                || producto.getId().toLowerCase(Locale.ROOT).contains(busqueda));

        ListProd.getSelectionModel().clearSelection();
    }

    // OBTENER LA CANTIDAD INTRODUCIDA.
    private int obtenerCantidad() {
        String texto = Cantidad.getText();

        if (texto == null || texto.isBlank()) {
            return 1;
        }

        try {
            int cantidad = Integer.parseInt(texto.trim());

            if (cantidad <= 0) {
                throw new IllegalArgumentException(
                        "La cantidad debe ser mayor que cero."
                );
            }

            return cantidad;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Introduce una cantidad entera válida."
            );
        }
    }

    // AÑADIR UNIDADES AL CARRITO TEMPORAL.
    @FXML
    private void añadirAlCarrito() {
        Product producto = ListProd.getSelectionModel().getSelectedItem();

        if (producto == null) {
            mostrarAviso("Selecciona un producto primero.");
            return;
        }

        try {
            int requested = obtenerCantidad();

            int current = carrito.getOrDefault(producto.getId(), 0);

            int updated = Math.addExact(current, requested);

            if (updated > producto.getStock()) {
                mostrarAviso("Stock insuficiente. Disponible: " + producto.getStock());
                return;
            }

            carrito.put(producto.getId(), updated);

            ListProd.refresh();
            Cantidad.clear();

        } catch (IllegalArgumentException e) {
            mostrarAviso(e.getMessage());
        } catch (ArithmeticException e) {
            mostrarAviso("La cantidad solicitada es demasiado grande.");
        }
    }

    // RESTAR UNIDADES DEL CARRITO TEMPORAL.
    @FXML
    private void eliminarDelCarrito() {
        Product producto = ListProd.getSelectionModel().getSelectedItem();

        if (producto == null) {
            mostrarAviso("Selecciona un producto primero.");
            return;
        }

        try {
            int requested = obtenerCantidad();

            int current = carrito.getOrDefault(producto.getId(), 0);

            if (current == 0) {
                mostrarAviso("Este producto no está en el carrito.");
                return;
            }
            if (requested > current) {
                mostrarAviso("No puedes eliminar más unidades de las que hay en el carrito. Actualmente: " + current);
                return;
            }
            int updated = current - requested;

            if (updated == 0) {
                carrito.remove(producto.getId());

            } else {
                carrito.put(producto.getId(), updated);
            }

            ListProd.refresh();
            Cantidad.clear();

        } catch (IllegalArgumentException e) {
            mostrarAviso(e.getMessage());
        }
    }

    // CONSULTAR EL CARRITO DESDE OTROS MÉTODOS.
    public Map<String, Integer> getCarrito() {
        return Map.copyOf(carrito);
    }

    @FXML
    private void openProductManagement() {
        if (!Session.isAdmin()) {
            return;
        }

        stockAlertManager.stop();

        try {
            AppNavigator.showProductManagement();

        } catch (Exception e) {
            e.printStackTrace();
            ButtonSound.playClick("error");
            mostrarError("No se pudo abrir la gestión de productos.");
        }
    }

    @FXML
    private void openReplenishment() {
        if (!Session.isAdmin()) {
            return;
        }

        stockAlertManager.stop();

        try {
            AppNavigator.showReabastecimiento();

        } catch (Exception e) {
            e.printStackTrace();
            ButtonSound.playClick("error");
            mostrarError("No se pudo abrir la pantalla de reabastecimiento.");
        }
    }

    // CERRAR SESIÓN.
    @FXML
    private void logout() {
        stockAlertManager.stop();

        Session.logout();

        try {
            AppNavigator.showLogin();

        } catch (Exception e) {
            e.printStackTrace();
            ButtonSound.playClick("error");
            mostrarError("No se pudo regresar al Login.");
        }
    }

    // ABRIR VENTANA DE VENTA.
    @FXML
    private void openShop() {
        stockAlertManager.stop();

        ButtonSound.playClick("Caja Registradora");

        try {
            AppNavigator.showVenta();

        } catch (Exception e) {
            e.printStackTrace();
            ButtonSound.playClick("error");
            mostrarError("No se pudo abrir la ventana de Venta.");
        }
    }

    // ABRIR REGISTRO.
    @FXML
    private void openRegister() {
        stockAlertManager.stop();

        if (!Session.isAdmin()) {
            return;
        }

        try {
            AppNavigator.showRegister();

        } catch (Exception e) {
            e.printStackTrace();
            ButtonSound.playClick("error");
            mostrarError("No se pudo abrir la ventana de Registro.");
        }
    }

    private void mostrarAviso(String mensaje) {
        new Alert(Alert.AlertType.WARNING, mensaje).show();
    }

    private void mostrarError(String mensaje) {
        new Alert(Alert.AlertType.ERROR, mensaje).show();
    }
}