package Controllers;

import Models.Product;
import Services.ProductService;
import Services.Session;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class RegistroController {
    @FXML private Button Out;
    @FXML private Button Back;

    @FXML private ListView<Product> ListProd;
    @FXML private TextField BarraBusqueda;

    @FXML private Button BtnBusqueda;
    @FXML private Button BtnAgregar;
    @FXML private Button BtnEliminar;

    private FilteredList<Product> productosFiltrados;

    @FXML
    public void initialize() {
        if (!Session.isAdmin()) {
            mostrarError("No tienes permisos para gestionar productos.");
            PlatformBack();
            return;
        }

        configurarLista();
        configurarBusqueda();
        cargarProductos();

        // Doble clic para modificar.
        ListProd.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                editarProducto();
            }
        });
    }

    // CONFIGURAR LA PRESENTACIÓN DE LOS PRODUCTOS.
    private void configurarLista() {
        ListProd.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Product producto, boolean empty) {
                super.updateItem(producto, empty);

                if (empty || producto == null) {
                    setText(null);
                    return;
                }

                setText(
                        producto.getName()
                                + "  |  " + producto.getCategory()
                                + "  |  $" + String.format(
                                Locale.forLanguageTag("es-CO"),
                                "%,.0f",
                                producto.getPrice()
                        )
                                + "  |  Stock: " + producto.getStock()
                                + "  |  Mínimo: " + producto.getStockMinimo()
                );
            }
        });
    }

    // CARGAR PRODUCTOS ACTIVOS.
    private void cargarProductos() {
        try {
            List<Product> productos = ProductService.getProducts();

            productosFiltrados = new FilteredList<>(
                    FXCollections.observableArrayList(productos),
                    producto -> true
            );

            ListProd.setItems(productosFiltrados);

        } catch (Exception e) {
            e.printStackTrace();

            mostrarError("No se pudieron cargar los productos.");
        }
    }

    // BÚSQUEDA EN TIEMPO REAL.
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

        productosFiltrados.setPredicate(producto -> {
            if (busqueda.isEmpty()) {
                return true;
            }

            return producto.getName()
                    .toLowerCase(Locale.ROOT)
                    .contains(busqueda)

                    || producto.getCategory()
                    .toLowerCase(Locale.ROOT)
                    .contains(busqueda)

                    || producto.getId()
                    .toLowerCase(Locale.ROOT)
                    .contains(busqueda);
        });
    }

    // AGREGAR PRODUCTO.
    @FXML
    private void agregarProducto() {
        if (!Session.isAdmin()) {
            mostrarError("Operación no autorizada.");
            return;
        }

        Dialog<ProductData> dialog = crearFormulario(
                "Agregar producto",
                null
        );

        Optional<ProductData> resultado = dialog.showAndWait();

        resultado.ifPresent(data -> {
            try {
                ProductService.addProduct(
                        data.nombre(),
                        data.categoria(),
                        data.precio(),
                        data.stock(),
                        data.stockMinimo()
                );

                cargarProductos();

                mostrarInformacion(
                        "El producto se guardó correctamente."
                );

            } catch (Exception e) {
                e.printStackTrace();
                mostrarError(e.getMessage());
            }
        });
    }

    // EDITAR PRODUCTO SELECCIONADO.
    private void editarProducto() {
        Product seleccionado = ListProd.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            return;
        }

        Dialog<ProductData> dialog = crearFormulario(
                "Modificar producto",
                seleccionado
        );

        Optional<ProductData> resultado = dialog.showAndWait();

        resultado.ifPresent(data -> {
            try {
                Product actualizado = new Product(
                        seleccionado.getId(),
                        data.nombre(),
                        data.categoria(),
                        data.precio(),
                        data.stock(),
                        data.stockMinimo()
                );

                ProductService.updateProduct(actualizado);

                cargarProductos();

                mostrarInformacion("El producto se modificó correctamente.");

            } catch (Exception e) {
                e.printStackTrace();
                mostrarError(e.getMessage());
            }
        });
    }

    // DESACTIVAR PRODUCTO.
    @FXML
    private void eliminarProducto() {
        if (!Session.isAdmin()) {
            mostrarError("Operación no autorizada.");
            return;
        }

        Product seleccionado = ListProd.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAviso("Selecciona un producto para eliminarlo.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);

        confirmacion.setTitle("Desactivar producto");
        confirmacion.setHeaderText("¿Deseas desactivar " + seleccionado.getName() + "?");

        confirmacion.setContentText(
                "El producto dejará de aparecer en el inventario, "
                        + "pero sus datos y su historial de auditoría se conservarán."
        );

        Optional<ButtonType> respuesta = confirmacion.showAndWait();

        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
            return;
        }

        try {
            ProductService.deactivateProduct(
                    seleccionado.getId()
            );

            cargarProductos();

            mostrarInformacion("El producto fue desactivado correctamente.");

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError(e.getMessage());
        }
    }

    // FORMULARIO REUTILIZABLE PARA CREAR Y MODIFICAR.
    private Dialog<ProductData> crearFormulario(
            String titulo, Product producto
    ) {
        Dialog<ProductData> dialog = new Dialog<>();

        dialog.setTitle(titulo);
        dialog.setHeaderText(
                producto == null
                        ? "Introduce los datos del nuevo producto."
                        : "Modifica los datos del producto seleccionado."
        );

        ButtonType guardar = new ButtonType(
                "Guardar", ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                guardar, ButtonType.CANCEL
        );

        TextField nombre = new TextField(
                producto == null ? "" : producto.getName()
        );

        TextField categoria = new TextField(
                producto == null ? "" : producto.getCategory()
        );

        TextField precio = new TextField(
                producto == null
                        ? ""
                        : String.valueOf(producto.getPrice())
        );

        TextField stock = new TextField(
                producto == null
                        ? ""
                        : String.valueOf(producto.getStock())
        );

        TextField stockMinimo = new TextField(
                producto == null
                        ? "5"
                        : String.valueOf(producto.getStockMinimo())
        );

        nombre.setPromptText("Nombre del producto");
        categoria.setPromptText("Categoría");
        precio.setPromptText("Precio");
        stock.setPromptText("Unidades disponibles");
        stockMinimo.setPromptText("Umbral de alerta");

        GridPane contenido = new GridPane();

        contenido.setHgap(12);
        contenido.setVgap(12);
        contenido.setStyle("-fx-padding: 15;");

        contenido.add(new Label("Nombre:"), 0, 0);
        contenido.add(nombre, 1, 0);

        contenido.add(new Label("Categoría:"), 0, 1);
        contenido.add(categoria, 1, 1);

        contenido.add(new Label("Precio:"), 0, 2);
        contenido.add(precio, 1, 2);

        contenido.add(new Label("Stock:"), 0, 3);
        contenido.add(stock, 1, 3);

        contenido.add(new Label("Stock mínimo:"), 0, 4);
        contenido.add(stockMinimo, 1, 4);

        dialog.getDialogPane().setContent(contenido);

        // Evitar aceptar el formulario con datos inválidos.
        Button botonGuardar = (Button) dialog.getDialogPane()
                .lookupButton(guardar);

        botonGuardar.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {
                    try {
                        validarFormulario(
                                nombre.getText(),
                                categoria.getText(),
                                precio.getText(),
                                stock.getText(),
                                stockMinimo.getText()
                        );

                    } catch (IllegalArgumentException e) {
                        event.consume();
                        mostrarAviso(e.getMessage());
                    }
                }
        );

        dialog.setResultConverter(button -> {
            if (button != guardar) {
                return null;
            }

            return new ProductData(
                    nombre.getText().trim(),
                    categoria.getText().trim(),
                    Double.parseDouble(precio.getText().trim()),
                    Integer.parseInt(stock.getText().trim()),
                    Integer.parseInt(stockMinimo.getText().trim())
            );
        });

        return dialog;
    }

    // VALIDAR LOS DATOS DEL FORMULARIO.
    private void validarFormulario(
            String nombre, String categoria, String precio,
            String stock, String stockMinimo
    ) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del producto es obligatorio."
            );
        }

        try {
            double valorPrecio = Double.parseDouble(precio.trim());

            if (!Double.isFinite(valorPrecio) || valorPrecio < 0) {
                throw new IllegalArgumentException(
                        "El precio debe ser válido y no negativo."
                );
            }

            int valorStock = Integer.parseInt(stock.trim());
            int valorMinimo = Integer.parseInt(stockMinimo.trim());

            if (valorStock < 0 || valorMinimo < 0) {
                throw new IllegalArgumentException(
                        "El stock y el mínimo no pueden ser negativos."
                );
            }

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Precio, stock y stock mínimo deben ser números válidos."
            );
        }
    }

    // REGRESAR AL INVENTARIO.
    @FXML
    private void backToProducts() {
        PlatformBack();
    }

    private void PlatformBack() {
        try {
            AppNavigator.showInventory();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("No se pudo regresar al inventario.");
        }
    }

    // CERRAR SESIÓN.
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
        new Alert(
                Alert.AlertType.WARNING, mensaje
        ).showAndWait();
    }

    private void mostrarError(String mensaje) {
        new Alert(
                Alert.AlertType.ERROR,
                mensaje == null ? "Ocurrió un error inesperado." : mensaje
        ).showAndWait();
    }

    private void mostrarInformacion(String mensaje) {
        new Alert(
                Alert.AlertType.INFORMATION, mensaje
        ).showAndWait();
    }

    private record ProductData(
            String nombre, String categoria, double precio,
            int stock, int stockMinimo
    ) {}
}
