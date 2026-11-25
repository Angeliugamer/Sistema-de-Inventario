package Controllers;

import Models.Purchase;
import Models.User;

import Services.ButtonSound;
import Services.JsonStorage;
import Services.PurchaseService;
import Services.Session;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryController {
    @FXML private Button Out;
    @FXML private Button Back;
    @FXML private Button BtnBuscar;
    @FXML private DatePicker DateFilter;
    @FXML private Label TotalDiaLabel;

    @FXML private TableView<Purchase> TablaVentas;
    @FXML private TableColumn<Purchase, String> ColProducto;
    @FXML private TableColumn<Purchase, Integer> ColCantidad;
    @FXML private TableColumn<Purchase, String> ColTotal;
    @FXML private TableColumn<Purchase, String> ColUsuario;
    @FXML private TableColumn<Purchase, Purchase> ColFecha;

    private final Map<String, String> nombresUsuarios = new HashMap<>();

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern(
                    "yyyy / MM / dd - h:mm a",
                    Locale.US
            );

    @FXML
    public void initialize() {
        if (!Session.isAdmin()) {
            mostrarError("No tienes permisos para consultar el historial de ventas.");

            Platform.runLater(this::backToProducts);
            return;
        }

        cargarNombresUsuarios();
        configurarColumnas();

        LocalDate hoy = LocalDate.now();
        DateFilter.setValue(hoy);

        cargarVentas(hoy);
    }

    // CARGAR LOS NOMBRES DE LOS USUARIOS
    private void cargarNombresUsuarios() {
        nombresUsuarios.clear();

        List<User> administradores = JsonStorage.readAdmins();
        List<User> vendedores = JsonStorage.readSellers();

        for (User user : administradores) {
            nombresUsuarios.put(
                    user.getId(),
                    user.getUsername()
            );
        }

        for (User user : vendedores) {
            nombresUsuarios.put(
                    user.getId(),
                    user.getUsername()
            );
        }
    }

    // CONFIGURAR LAS COLUMNAS
    private void configurarColumnas() {
        Locale localeCO = Locale.forLanguageTag("es-CO");

        // La columna recibe el objeto Purchase completo.
        ColFecha.setCellValueFactory(cell ->
                new SimpleObjectProperty<>(cell.getValue())
        );

        // Personalizar la fecha para poner las barras en negrita.
        ColFecha.setCellFactory(column ->
                new TableCell<Purchase, Purchase>() {
                    @Override
                    protected void updateItem(Purchase purchase, boolean empty) {
                        super.updateItem(purchase, empty);

                        if (empty || purchase == null) {
                            setText(null);
                            setGraphic(null);
                            return;
                        }

                        try {
                            LocalDateTime fecha = LocalDateTime.parse(purchase.getDate());
                            String fechaFormateada = fecha.format(FORMATO_FECHA);
                            String[] partes = fechaFormateada.split(" / | - ");

                            if (partes.length != 4) {
                                setText(fechaFormateada);
                                setGraphic(null);
                                return;
                            }

                            Text anio = new Text(partes[0]);
                            Text barra1 = new Text(" / ");
                            Text mes = new Text(partes[1]);
                            Text barra2 = new Text(" / ");
                            Text dia = new Text(partes[2]);
                            Text hora = new Text(" - " + partes[3]);

                            barra1.setStyle("-fx-font-weight: bold;");
                            barra2.setStyle("-fx-font-weight: bold;");

                            TextFlow contenido = new TextFlow(
                                    anio,
                                    barra1,
                                    mes,
                                    barra2,
                                    dia,
                                    hora
                            );

                            setText(null);
                            setGraphic(contenido);

                        } catch (Exception e) {
                            setGraphic(null);
                            setText(purchase.getDate());
                        }
                    }
                }
        );

        ColCantidad.setCellValueFactory(cell ->
                new SimpleIntegerProperty(
                        cell.getValue().getQuantity()
                ).asObject()
        );

        ColTotal.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        String.format(
                                localeCO,
                                "$%,.0f",
                                cell.getValue().getTotal()
                        )
                )
        );

        // Mostrar el nombre en lugar del ID.
        ColUsuario.setCellValueFactory(cell -> {
            String userId = cell.getValue().getUserId();

            String username = nombresUsuarios.get(userId);

            if (username == null || username.isBlank()) {
                username = "Usuario no encontrado";
            }

            return new SimpleStringProperty(username);
        });

        // La columna recibe la compra completa.
        ColFecha.setCellValueFactory(cell ->
                new SimpleObjectProperty<>(cell.getValue())
        );

        // Personalizar la fecha para poner las barras en negrita.
        ColFecha.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Purchase purchase, boolean empty) {
                super.updateItem(purchase, empty);

                if (empty || purchase == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                try {
                    LocalDateTime fecha = LocalDateTime.parse(
                            purchase.getDate()
                    );

                    String fechaFormateada =
                            fecha.format(FORMATO_FECHA);

                    String[] partes = fechaFormateada.split(" / | - ");

                    if (partes.length != 4) {
                        setText(fechaFormateada);
                        setGraphic(null);
                        return;
                    }

                    Text anio = new Text(partes[0]);
                    Text barra1 = new Text(" / ");
                    Text mes = new Text(partes[1]);
                    Text barra2 = new Text(" / ");
                    Text dia = new Text(partes[2]);
                    Text hora = new Text(" - " + partes[3]);

                    // Negrita solamente para las barras.
                    barra1.setStyle("-fx-font-weight: bold;");
                    barra2.setStyle("-fx-font-weight: bold;");

                    TextFlow contenido = new TextFlow(
                            anio,
                            barra1,
                            mes,
                            barra2,
                            dia,
                            hora
                    );

                    setText(null);
                    setGraphic(contenido);

                } catch (Exception e) {
                    // Si una fecha antigua o incorrecta no puede
                    // interpretarse, mostrar el valor original.
                    setGraphic(null);
                    setText(purchase.getDate());
                }
            }
        });
    }

    // BUSCAR VENTAS POR FECHA
    @FXML
    private void buscarPorFecha() {
        ButtonSound.playClick();

        LocalDate fecha = DateFilter.getValue();

        if (fecha == null) {
            fecha = LocalDate.now();
            DateFilter.setValue(fecha);
        }

        cargarVentas(fecha);
    }

    private void cargarVentas(LocalDate fecha) {
        try {
            List<Purchase> ventas = PurchaseService.getPurchasesByDate(fecha);
            TablaVentas.setItems(FXCollections.observableArrayList(ventas));

            double sumaTotal = ventas.stream()
                    .mapToDouble(Purchase::getTotal)
                    .sum();

            TotalDiaLabel.setText(
                    String.format(
                            Locale.forLanguageTag("es-CO"),
                            "Total del día: $%,.0f",
                            sumaTotal
                    )
            );

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("No se pudieron cargar las ventas de la fecha seleccionada.");
        }
    }

    @FXML
    private void backToProducts() {
        try {
            ButtonSound.playClick();
            AppNavigator.returnFromHistory();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void logout() {
        try {
            ButtonSound.playClick();
            Session.logout();
            AppNavigator.showLogin();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        alert.showAndWait();
    }
}