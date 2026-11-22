package Services;

import Models.Product;

import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import javafx.util.Duration;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class StockAlertManager {
    // DURACIONES DE LA NOTIFICACIÓN.
    private static final double ESPERA_INICIAL = 5;
    private static final double TIEMPO_VISIBLE = 15;
    private static final double DURACION_DESLIZAMIENTO = 0.7;
    private static final double INTERVALO_REVISION = 10;

    private StackPane root;

    private final Queue<Product> cola = new ArrayDeque<>();

    // Evita repetir notificaciones del mismo producto.
    private final Set<String> productosNotificados = new HashSet<>();
    private final Set<String> productosEnCola = new HashSet<>();

    private Timeline monitor;

    private boolean mostrandoAlerta = false;
    private boolean detenido = false;

    // INICIAR EL SISTEMA.
    public void start(StackPane root) {
        if (root == null) {
            throw new IllegalArgumentException(
                    "La ventana principal no puede ser nula."
            );
        }

        this.root = root;
        this.detenido = false;

        revisarInventario();

        monitor = new Timeline(
                new KeyFrame(
                        Duration.seconds(INTERVALO_REVISION),
                        event -> revisarInventario()
                )
        );

        monitor.setCycleCount(Timeline.INDEFINITE);
        monitor.play();
    }

    // REVISAR LOS PRODUCTOS CON POCO STOCK.
    private void revisarInventario() {
        if (detenido) {
            return;
        }

        List<Product> productos = ProductService.getProducts();

        for (Product producto : productos) {
            String id = producto.getId();

            if (producto.getStock() <= producto.getStockMinimo()) {
                if (!productosNotificados.contains(id)) {

                    cola.offer(producto);

                    productosNotificados.add(id);
                    productosEnCola.add(id);
                }

            } else {
                // Si se repone el producto, podrá volver
                // a generar una alerta en el futuro.
                productosNotificados.remove(id);
                productosEnCola.remove(id);

                cola.removeIf(p -> p.getId().equals(id));
            }
        }

        mostrarSiguiente();
    }

    // PROGRAMAR LA SIGUIENTE NOTIFICACIÓN.
    private void mostrarSiguiente() {
        if (detenido || mostrandoAlerta || cola.isEmpty()) {
            return;
        }

        mostrandoAlerta = true;

        PauseTransition espera = new PauseTransition(
                Duration.seconds(ESPERA_INICIAL)
        );

        espera.setOnFinished(event -> {

            if (detenido) {
                mostrandoAlerta = false;
                return;
            }

            Product producto = cola.poll();

            if (producto == null) {
                mostrandoAlerta = false;
                return;
            }

            productosEnCola.remove(producto.getId());

            // Consultar el stock actualizado antes de mostrar.
            Product actual = ProductService.getProducts()
                    .stream()
                    .filter(p -> p.getId().equals(producto.getId()))
                    .findFirst()
                    .orElse(null);

            if (actual == null || actual.getStock() > actual.getStockMinimo()) {
                productosNotificados.remove(producto.getId());

                mostrandoAlerta = false;
                mostrarSiguiente();
                return;
            }

            mostrarAlerta(actual);
        });

        espera.play();
    }

    // CREAR Y ANIMAR LA NOTIFICACIÓN.
    private void mostrarAlerta(Product producto) {
        if (detenido) {
            mostrandoAlerta = false;
            return;
        }

        String mensaje;

        if (producto.getStock() == 0) {
            mensaje = "Alerta: " + producto.getName()
                    + " está agotado.";
        } else {
            mensaje = "Alerta: " + producto.getName()
                    + " tiene pocas unidades. Quedan "
                    + producto.getStock() + ".";
        }

        // CARGAR LA IMAGEN DE ALERTA.
        var imageURL = StockAlertManager.class.getResource(
                "/Shared/2 Lista Productos/Alert.png"
        );

        if (imageURL == null) {
            mostrandoAlerta = false;

            throw new IllegalStateException(
                    "No se encontró Alert.png en Images/Shared/2 Lista Productos."
            );
        }

        ImageView fondo = new ImageView(new Image(imageURL.toExternalForm()));

        fondo.setFitWidth(380);
        fondo.setFitHeight(115);
        fondo.setPreserveRatio(true);
        fondo.setSmooth(true);

        // TEXTO DINÁMICO.
        Label texto = new Label(mensaje);

        texto.setWrapText(true);
        texto.setMaxWidth(290);
        texto.setAlignment(Pos.CENTER_LEFT);

        texto.setStyle(
                "-fx-font-family: Arial;" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #222222;"
        );

        StackPane alerta = new StackPane(fondo, texto);

        alerta.setPrefSize(380, 115);
        alerta.setMaxSize(380, 115);

        StackPane.setAlignment(texto, Pos.CENTER);
        StackPane.setMargin(texto, new Insets(5, 35, 5, 55));

        StackPane.setAlignment(alerta, Pos.BOTTOM_LEFT);
        StackPane.setMargin(alerta, new Insets(0, 0, 25, 25));

        root.getChildren().add(alerta);

        // ENTRADA: IZQUIERDA -> DERECHA.
        TranslateTransition entrada =
                new TranslateTransition(
                        Duration.seconds(DURACION_DESLIZAMIENTO),
                        alerta
                );

        entrada.setFromX(-430);
        entrada.setToX(0);

        entrada.setOnFinished(event -> mantenerVisible(alerta));

        entrada.play();
    }

    // MANTENER LA ALERTA VISIBLE DURANTE 15 SEGUNDOS.
    private void mantenerVisible(StackPane alerta) {
        if (detenido) {
            root.getChildren().remove(alerta);
            mostrandoAlerta = false;
            return;
        }

        PauseTransition permanencia =
                new PauseTransition(
                        Duration.seconds(TIEMPO_VISIBLE)
                );

        permanencia.setOnFinished(event -> retirarAlerta(alerta));

        permanencia.play();
    }

    // SALIDA: DERECHA -> IZQUIERDA.
    private void retirarAlerta(StackPane alerta) {
        TranslateTransition salida =
                new TranslateTransition(
                        Duration.seconds(DURACION_DESLIZAMIENTO),
                        alerta
                );

        salida.setFromX(0);
        salida.setToX(-430);

        salida.setOnFinished(event -> {
            root.getChildren().remove(alerta);

            mostrandoAlerta = false;

            mostrarSiguiente();
        });

        salida.play();
    }

    // DETENER EL SISTEMA.
    public void stop() {
        detenido = true;

        if (monitor != null) {
            monitor.stop();
        }

        cola.clear();
        productosEnCola.clear();
    }
}
