package Controllers;

import Models.Product;

import Services.CategoryService;
import Services.ProductService;
import Services.Session;

import Components.ProductListCell;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import javafx.scene.text.TextAlignment;

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
    @FXML private Button BtnCrearCategoria;

    private FilteredList<Product> productosFiltrados;

    @FXML
    public void initialize() {
        if (!Session.isAdmin()) {
            mostrarError("No tienes permisos para gestionar productos.");
            Platform.runLater(this::backToProducts);
            return;
        }

        configurarLista();
        configurarBusqueda();
        cargarProductos();
    }

    // CONFIGURAR LA PRESENTACIÓN DE LOS PRODUCTOS.

    private void configurarLista() {
        ListProd.setCellFactory(list ->
                new ProductListCell(
                        ProductListCell.Mode.REGISTRATION
                )
        );

        ListProd.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                editarProducto();
            }
        });
    }



    // CARGAR PRODUCTOS ACTIVOS.
    private void cargarProductos() {
        try {
            productosFiltrados = new FilteredList<>(
                    FXCollections.observableArrayList(ProductService.getProducts()),
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

        productosFiltrados.setPredicate(producto -> busqueda.isEmpty()
                || producto.getName().toLowerCase(Locale.ROOT).contains(busqueda)
                || producto.getCategory().toLowerCase(Locale.ROOT).contains(busqueda)
                || producto.getId().toLowerCase(Locale.ROOT).contains(busqueda));
    }

    // CREAR CATEGORÍA NUEVA.
    @FXML
    private void crearCategoria() {
        if (!Session.isAdmin()) {
            mostrarError("Operación no autorizada.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Crear categoría");
        dialog.setHeaderText("Añadir una categoría al catálogo");
        dialog.setContentText("Nombre de la categoría:");
        dialog.showAndWait().ifPresent(nombre -> {
            try {
                String creada = CategoryService.addCategory(nombre);
                mostrarInformacion("La categoría \"" + creada + "\" se creó correctamente.");
            } catch (Exception e) {
                mostrarAviso(e.getMessage() == null
                        ? "No se pudo crear la categoría."
                        : e.getMessage());
            }
        });
    }

    // AGREGAR PRODUCTO.
    @FXML
    private void agregarProducto() {
        if (!Session.isAdmin()) {
            mostrarError("Operación no autorizada.");
            return;
        }

        if (CategoryService.getCategories().isEmpty()) {
            mostrarAviso("Primero debes crear al menos una categoría.");
            return;
        }

        Optional<ProductData> resultado = crearFormulario(
                "Agregar producto", null
        ).showAndWait();

        resultado.ifPresent(data -> {
            try {
                ProductService.addProduct(data.nombre(), data.categoria(),
                        data.precio(), data.stock(), data.stockMinimo());
                cargarProductos();
                mostrarInformacion("El producto se guardó correctamente.");
            } catch (Exception e) {
                e.printStackTrace();
                mostrarError(mensajeError(e, "No se pudo guardar el producto."));
            }
        });
    }

    // EDITAR PRODUCTO SELECCIONADO.
    private void editarProducto() {
        if (!Session.isAdmin()) {
            mostrarError("Operación no autorizada.");
            return;
        }

        Product seleccionado = ListProd.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            return;
        }

        Optional<ProductData> resultado = crearFormulario(
                "Modificar producto",
                seleccionado
        ).showAndWait();

        resultado.ifPresent(data -> {
            try {
                Product actualizado = new Product.Builder()
                        .id(seleccionado.getId())
                        .name(data.nombre())
                        .category(data.categoria())
                        .price(data.precio())
                        .stock(data.stock())
                        .stockMinimo(data.stockMinimo())
                        .active(seleccionado.isActive())
                        .build();

                ProductService.updateProduct(actualizado);
                cargarProductos();
                mostrarInformacion("El producto se modificó correctamente.");
            } catch (Exception e) {
                e.printStackTrace();
                mostrarError(mensajeError(e, "No se pudo modificar el producto."));
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

        Label mensaje = new Label(
                "El producto dejará de aparecer en el inventario, "
                        + "pero sus datos y su historial de auditoría se conservarán."
        );

        mensaje.setWrapText(true);
        mensaje.setPrefWidth(380);
        mensaje.setMaxWidth(380);

        confirmacion.getDialogPane().setContent(mensaje);
        confirmacion.getDialogPane().setPrefWidth(460);

        Optional<ButtonType> respuesta = confirmacion.showAndWait();

        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
            return;
        }

        try {
            ProductService.deactivateProduct(seleccionado.getId());
            cargarProductos();
            mostrarInformacion("El producto fue desactivado correctamente.");

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError(mensajeError(e, "No se pudo desactivar el producto."));
        }
    }

    // FORMULARIO REUTILIZABLE PARA CREAR Y MODIFICAR.
    private Dialog<ProductData> crearFormulario(String titulo, Product producto) {
        Dialog<ProductData> dialog = new Dialog<>();

        dialog.setTitle(titulo);
        dialog.setHeaderText(producto == null
                ? "Introduce los datos del nuevo producto."
                : "Modifica los datos del producto seleccionado."
        );

        ButtonType guardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardar, ButtonType.CANCEL);

        TextField nombre = new TextField(
                producto == null ? "" : producto.getName()
        );

        ComboBox<String> categoria = new ComboBox<>(
                FXCollections.observableArrayList(CategoryService.getCategories())
        );

        categoria.setPromptText("Selecciona una categoría");
        categoria.setMaxWidth(Double.MAX_VALUE);

        if (producto != null) {
            categoria.setValue(CategoryService.resolveCategory(producto.getCategory()));
        }

        TextField precio = new TextField(producto == null
                ? ""
                : String.valueOf(producto.getPrice())
        );

        TextField stock = new TextField(producto == null
                ? ""
                : String.valueOf(producto.getStock())
        );

        TextField stockMinimo = new TextField(producto == null
                ? "5"
                : String.valueOf(producto.getStockMinimo())
        );

        nombre.setPromptText("Nombre del producto");
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

        botonGuardar.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                validarFormulario(
                        nombre.getText(),
                        categoria.getValue(),
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
                    nombre.getText().trim(), categoria.getValue(),
                    Double.parseDouble(precio.getText().trim()),
                    Integer.parseInt(stock.getText().trim()),
                    Integer.parseInt(stockMinimo.getText().trim())
            );
        });

        return dialog;
    }

    // VALIDAR LOS DATOS DEL FORMULARIO.
    private void validarFormulario(String nombre, String categoria,
                                   String precio, String stock, String stockMinimo) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }

        if (categoria == null || categoria.isBlank()) {
            throw new IllegalArgumentException("Selecciona una categoría.");
        }

        if (precio == null || precio.isBlank() || stock == null || stock.isBlank()
                || stockMinimo == null || stockMinimo.isBlank()) {
            throw new IllegalArgumentException(
                    "Debes completar el precio, el stock y el stock mínimo."
            );
        }

        try {
            double valorPrecio = Double.parseDouble(precio.trim());

            if (!Double.isFinite(valorPrecio) || valorPrecio < 0) {
                throw new IllegalArgumentException(
                        "El precio debe ser un número válido y no negativo."
                );
            }

            int valorStock = Integer.parseInt(stock.trim());
            int valorMinimo = Integer.parseInt(stockMinimo.trim());

            if (valorStock < 0 || valorMinimo < 0) {
                throw new IllegalArgumentException(
                        "El stock y el stock mínimo no pueden ser negativos."
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

    // OBTENER UN MENSAJE DE ERROR LEGIBLE.
    private String mensajeError(Exception e, String predeterminado) {
        String mensaje = e.getMessage();

        return mensaje == null || mensaje.isBlank()
                ? predeterminado
                : mensaje;
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

    private record ProductData(String nombre, String categoria, double precio,
            int stock, int stockMinimo) {
    }
}
