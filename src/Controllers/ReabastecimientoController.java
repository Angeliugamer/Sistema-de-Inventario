package Controllers;

import Models.Product;
import Services.ProductService;
import Services.Session;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ReabastecimientoController {
    @FXML private Button Out;
    @FXML private Button Back;
    @FXML private Button BtnBusqueda;
    @FXML private Button BtnReabastecer;
    @FXML private ListView<Product> ListProd;
    @FXML private TextField BarraBusqueda;
    @FXML private TextField Cantidad;

    private FilteredList<Product> productosFiltrados;

    @FXML
    public void initialize() {
        if (!Session.isAdmin()) {
            mostrarError("No tienes permisos para reabastecer productos.");
            javafx.application.Platform.runLater(this::backToProducts);
            return;
        }

        configurarLista();
        configurarBusqueda();
        cargarProductos();
    }

    private void configurarLista() {
        ListProd.setCellFactory(list -> new ListCell<>() {
            {
                setOnMouseEntered(event -> {
                    if (!isEmpty()) {
                        setStyle("-fx-background-color: #D3D3D3; -fx-text-fill: black;");
                    }
                });

                setOnMouseExited(event -> actualizarEstilo(getItem()));
            }

            @Override
            protected void updateItem(Product producto, boolean empty) {
                super.updateItem(producto, empty);

                if (empty || producto == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(producto.getName()
                        + "  |  " + producto.getCategory()
                        + "  |  Stock: " + producto.getStock()
                        + "  |  Mínimo: " + producto.getStockMinimo());

                actualizarEstilo(producto);
            }

            private void actualizarEstilo(Product producto) {
                if (producto == null) {
                    setStyle("");
                } else if (isHover()) {
                    setStyle("-fx-background-color: #D3D3D3; -fx-text-fill: black;");
                } else if (producto.getStock() == 0) {
                    setStyle("-fx-background-color: #F4CCCC; -fx-text-fill: black;");
                } else if (producto.getStock() <= producto.getStockMinimo()) {
                    setStyle("-fx-background-color: #FFF2CC; -fx-text-fill: black;");
                } else {
                    setStyle("-fx-background-color: white; -fx-text-fill: black;");
                }
            }
        });
    }

    private void cargarProductos() {
        try {
            List<Product> products = ProductService.getProducts();
            productosFiltrados = new FilteredList<>(
                    FXCollections.observableArrayList(products),
                    product -> true
            );
            ListProd.setItems(productosFiltrados);

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("No se pudieron cargar los productos.");
        }
    }

    private void configurarBusqueda() {
        BarraBusqueda.textProperty().addListener(
                (observable, anterior, actual) -> aplicarBusqueda(actual)
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

        String search = texto == null
                ? ""
                : texto.trim().toLowerCase(Locale.ROOT);

        productosFiltrados.setPredicate(product -> search.isEmpty()
                || product.getName().toLowerCase(Locale.ROOT).contains(search)
                || product.getCategory().toLowerCase(Locale.ROOT).contains(search)
                || product.getId().toLowerCase(Locale.ROOT).contains(search));
    }

    @FXML
    private void reabastecerProducto() {
        if (!Session.isAdmin()) {
            mostrarError("Operación no autorizada.");
            return;
        }

        Product selected = ListProd.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostrarAviso("Selecciona el producto que deseas reabastecer.");
            return;
        }

        String text = Cantidad.getText();
        if (text == null || text.isBlank()) {
            mostrarAviso("Introduce la cantidad que deseas agregar.");
            return;
        }

        final int quantity;
        try {
            quantity = Integer.parseInt(text.trim());
            if (quantity <= 0) {
                mostrarAviso("La cantidad debe ser mayor que cero.");
                return;
            }
        } catch (NumberFormatException e) {
            mostrarAviso("Introduce una cantidad entera válida.");
            return;
        }

        try {
            ProductService.replenishStock(selected.getId(), quantity);
            Cantidad.clear();
            cargarProductos();
            mostrarInformacion("Se agregaron " + quantity
                    + " unidades a " + selected.getName() + ".");

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError(e.getMessage() == null
                    ? "No se pudo reabastecer el producto."
                    : e.getMessage());
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

    @FXML
    private void logout() {
        Session.logout();
        try {
            AppNavigator.showLogin();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("No se pudo cerrar la sesión.");
        }
    }

    private void mostrarAviso(String mensaje) {
        new Alert(Alert.AlertType.WARNING, mensaje).showAndWait();
    }

    private void mostrarError(String mensaje) {
        new Alert(Alert.AlertType.ERROR, mensaje).showAndWait();
    }

    private void mostrarInformacion(String mensaje) {
        new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK).showAndWait();
    }
}
