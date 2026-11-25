package Controllers;

import Services.CalculadoraVenta;
import Services.Session;
import Services.ButtonSound;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class VentaController {
    @FXML
    private Button Historial;
    private final CalculadoraVenta calculadoraVenta = new CalculadoraVenta();

    @FXML
    public void initialize() {
        boolean admin = Session.isAdmin();

        Historial.setVisible(admin);
        Historial.setManaged(admin);
    }

    @FXML
    public void purchase() {

    }

    @FXML
    private void openHistory() {
        if (!Session.isAdmin()) {
            return;
        }

        // ButtonSound.playClick();

        // Aquí conectaremos History.fxml cuando lo creemos.
    }

    @FXML
    private void logout() {
        // ButtonSound.playClick();
        Session.logout();

        try {
            AppNavigator.showLogin();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void backToProducts() {
        // ButtonSound.playClick();

        try {
            AppNavigator.showInventory();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
