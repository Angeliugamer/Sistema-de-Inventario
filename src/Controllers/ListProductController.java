package Controllers;

import Services.Session;
import Services.ButtonSound;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;

public class ListProductController {
    @FXML private Button Out;
    @FXML private Button Shop;
    @FXML private Button Regist;
    @FXML private Button Historial;

    @FXML
    public void initialize() {
        boolean admin = Session.isAdmin();

        Regist.setVisible(admin);
        Regist.setManaged(admin);

        Historial.setVisible(admin);
        Historial.setManaged(admin);
    }

    // CERRAR SESIÓN Y VOLVER AL LOGIN
    @FXML
    private void logout() {
        // ButtonSound.playClick();
        Session.logout();

        try {
            AppNavigator.showLogin();

        } catch (Exception e) {
            e.printStackTrace();

            ButtonSound.playClick("error");

            new Alert(
                    Alert.AlertType.ERROR, "No se pudo regresar al Login."
            ).show();

        }
    }

    // ABRIR VENTANA DE VENTA
    @FXML
    private void openShop() {
        ButtonSound.playClick("Caja Registradora");

        try {
            AppNavigator.showVenta();

        } catch (Exception e) {
            e.printStackTrace();

            ButtonSound.playClick("error");

            new Alert(
                    Alert.AlertType.ERROR, "No se pudo abrir la ventana de Venta."
            ).show();
        }
    }

    // ABRIR VENTANA DE REGISTRO
    @FXML
    private void openRegister() {
        if (!Session.isAdmin()) {
            return;
        }

        // ButtonSound.playClick();

        try {
            AppNavigator.showRegister();

        } catch (Exception e) {
            e.printStackTrace();

            ButtonSound.playClick("error");

            new Alert(
                    Alert.AlertType.ERROR, "No se pudo abrir la ventana de Registro."
            ).show();
        }
    }
}
