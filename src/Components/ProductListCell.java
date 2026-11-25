package Components;

import Models.Product;

import javafx.geometry.Insets;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.util.Locale;
import java.util.Map;

public class ProductListCell extends ListCell<Product> {
    public enum Mode {
        INVENTORY,
        REGISTRATION,
        REPLENISHMENT
    }

    private static final Color COLOR_NORMAL = Color.WHITE;
    private static final Color COLOR_STOCK_BAJO = Color.web("#FFF2CC");
    private static final Color COLOR_SIN_STOCK = Color.web("#F4CCCC");
    private static final Color COLOR_HOVER = Color.web("#D3D3D3");
    private static final Color COLOR_SELECTED = Color.web("#B4C7E7");
    private static final Color COLOR_CARRITO = Color.LIGHTGREEN;

    private final Mode mode;
    private final Map<String, Integer> carrito;

    private final GridPane fila = new GridPane();

    private final Label nombre = new Label();
    private final Label categoria = new Label();
    private final Label precio = new Label();
    private final Label stock = new Label();
    private final Label extra = new Label();

    public ProductListCell(Mode mode) {
        this(mode, null);
    }

    public ProductListCell(Mode mode, Map<String, Integer> carrito) {
        this.mode = mode;
        this.carrito = carrito;

        configurarFila();

        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        selectedProperty().addListener(
                (observable, anterior, seleccionado) ->
                        actualizarEstilo()
        );

        hoverProperty().addListener(
                (observable, anterior, encima) ->
                        actualizarEstilo()
        );
    }

    private void configurarFila() {
        fila.setHgap(8);
        fila.setVgap(0);

        double[] porcentajes = {30, 20, 18, 16, 16};

        for (double porcentaje : porcentajes) {
            ColumnConstraints columna = new ColumnConstraints();
            columna.setPercentWidth(porcentaje);
            columna.setHgrow(Priority.ALWAYS);
            fila.getColumnConstraints().add(columna);
        }

        configurarLabel(nombre);
        configurarLabel(categoria);
        configurarLabel(precio);
        configurarLabel(stock);
        configurarLabel(extra);

        fila.add(nombre, 0, 0);
        fila.add(categoria, 1, 0);
        fila.add(precio, 2, 0);
        fila.add(stock, 3, 0);
        fila.add(extra, 4, 0);
    }

    private void configurarLabel(Label label) {
        label.setMaxWidth(Double.MAX_VALUE);
        label.setTextAlignment(TextAlignment.LEFT);
        label.setStyle(
                "-fx-padding: 5 2 5 2;" +
                        "-fx-text-fill: black;"
        );
    }

    @Override
    protected void updateItem(Product producto, boolean empty) {
        super.updateItem(producto, empty);

        if (empty || producto == null) {
            setText(null);
            setGraphic(null);
            setBackground(Background.EMPTY);
            setStyle("");
            return;
        }

        nombre.setText(producto.getName());
        categoria.setText(producto.getCategory());

        precio.setText(String.format(
                Locale.forLanguageTag("es-CO"),
                "$%,.0f",
                producto.getPrice()
        ));

        stock.setText(mode == Mode.INVENTORY
                ? String.valueOf(producto.getStock())
                : "Stock: " + producto.getStock()
        );

        switch (mode) {
            case INVENTORY -> {
                int unidades = obtenerCantidadCarrito(producto);
                extra.setText(unidades > 0 ? String.valueOf(unidades) : "");
            }

            case REGISTRATION, REPLENISHMENT -> {
                extra.setText("Mínimo: " + producto.getStockMinimo());
            }
        }

        setGraphic(fila);
        actualizarEstilo();
    }

    private int obtenerCantidadCarrito(Product producto) {
        if (carrito == null) {
            return 0;
        }

        return carrito.getOrDefault(producto.getId(), 0);
    }

    private void actualizarEstilo() {
        Product producto = getItem();

        if (producto == null || isEmpty()) {
            setBackground(Background.EMPTY);
            setStyle("");
            return;
        }

        Color color;

        // El hover solo afecta mientras el puntero está encima.
        if (isHover()) {
            color = COLOR_HOVER;

            // La selección se conserva al retirar el puntero.
        } else if (isSelected()) {
            color = COLOR_SELECTED;

            // Los productos del carrito siempre tienen fondo azul claro.
        } else if (mode == Mode.INVENTORY
                && obtenerCantidadCarrito(producto) > 0) {
            color = COLOR_CARRITO;

        } else if (producto.getStock() == 0) {
            color = COLOR_SIN_STOCK;

        } else if (producto.getStock() <= producto.getStockMinimo()) {
            color = COLOR_STOCK_BAJO;

        } else {
            color = COLOR_NORMAL;
        }

        setBackground(new Background(
                new BackgroundFill(
                        color,
                        CornerRadii.EMPTY,
                        Insets.EMPTY
                )
        ));

        // Evita que el estilo de selección predeterminado oscurezca la fila.
        setStyle("-fx-background-color: " + toRgb(color) + ";");
    }

    private String toRgb(Color color) {
        return String.format(
                Locale.ROOT,
                "#%02X%02X%02X",
                (int) Math.round(color.getRed() * 255),
                (int) Math.round(color.getGreen() * 255),
                (int) Math.round(color.getBlue() * 255)
        );
    }
}
