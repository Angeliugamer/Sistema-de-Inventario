package Controllers;

import Models.Purchase;

import Services.ButtonSound;
import Services.PurchaseService;
import Services.Session;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

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
    @FXML private TableColumn<Purchase, String> ColFecha;

    @FXML
    public void initialize() {
        if (!Session.isAdmin()) {
            mostrarError("No tienes permisos para consultar el historial de ventas.");
            Platform.runLater(this::backToProducts);
            return;
        }

        configurarColumnas();

        LocalDate hoy = LocalDate.now();
        DateFilter.setValue(hoy);
        cargarVentas(hoy);
    }

    private void configurarColumnas() {
        Locale localeCO = Locale.forLanguageTag("es-CO");

        ColProducto.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getProduct()));

        ColCantidad.setCellValueFactory(cell ->
                new SimpleIntegerProperty(cell.getValue().getQuantity()).asObject());

        ColTotal.setCellValueFactory(cell ->
                new SimpleStringProperty(String.format(localeCO, "$%,.0f", cell.getValue().getTotal())));

        ColUsuario.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getUserId()));

        ColFecha.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDate()));
    }

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

            TotalDiaLabel.setText(String.format(
                    Locale.forLanguageTag("es-CO"),
                    "Total del día: $%,.0f",
                    sumaTotal
            ));

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
            mostrarError("No se pudo regresar a la pantalla anterior.");
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
            mostrarError("No se pudo cerrar la sesión.");
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