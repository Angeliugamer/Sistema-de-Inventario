package Controllers;

import Main.MainApp;
import Models.User;
import Services.Session;
import Services.UserService;

import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.Optional;

public class LoginController {
    @FXML private ImageView picosSup;
    @FXML private ImageView picosInf;

    @FXML private TextField UsserField;

    @FXML private PasswordField AddresField;
    @FXML private TextField PasswordVisibleField;

    @FXML private ImageView EyeClosed;
    @FXML private ImageView EyeOpen;

    @FXML private Label ErrorLabel;

    private boolean passwordVisible = false;

    @FXML
    public void initialize() {
        animatePicos();

        // Configurar el campo de contraseña
        PasswordVisibleField.setVisible(false);
        PasswordVisibleField.setManaged(false);

        EyeOpen.setVisible(false);
        EyeOpen.setManaged(false);

        ErrorLabel.setVisible(false);
        ErrorLabel.setManaged(false);

        // Sincronizar los campos de contraseña
        AddresField.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        PasswordVisibleField.setText(newValue)
        );

        PasswordVisibleField.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (!newValue.equals(AddresField.getText())) {
                        AddresField.setText(newValue);
                    }
                }
        );
    }

    // ANIMACIONES (porfavor no lo quiten)

    private void animatePicos() {
        // Superior: derecha a izquierda
        TranslateTransition superior =
                new TranslateTransition(
                        Duration.seconds(30),
                        picosSup
                );

        superior.setByX(-2410);
        superior.setCycleCount(Animation.INDEFINITE);
        superior.setAutoReverse(false);

        // Inferior: izquierda a derecha
        TranslateTransition inferior =
                new TranslateTransition(
                        Duration.seconds(30),
                        picosInf
                );

        inferior.setByX(2410);
        inferior.setCycleCount(Animation.INDEFINITE);
        inferior.setAutoReverse(false);

        superior.play();
        inferior.play();
    }

    // MOSTRAR / OCULTAR CONTRASEÑA

    @FXML
    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;

        AddresField.setVisible(!passwordVisible);
        AddresField.setManaged(!passwordVisible);

        PasswordVisibleField.setVisible(passwordVisible);
        PasswordVisibleField.setManaged(passwordVisible);

        EyeClosed.setVisible(!passwordVisible);
        EyeClosed.setManaged(!passwordVisible);

        EyeOpen.setVisible(passwordVisible);
        EyeOpen.setManaged(passwordVisible);
    }

    // INICIAR SESIÓN

    @FXML
    private void login() {
        String username = UsserField.getText().trim();

        String password = passwordVisible
                ? PasswordVisibleField.getText()
                : AddresField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Completa todos los campos.");
            return;
        }

        User user = UserService.authenticate(
                username, password
        );

        if (user == null) {
            showError("Usuario o contraseña incorrectos.");
            return;
        }

        // Guardar sesión
        Session.login(user);

        try {
            MainApp.showInventory();

        } catch (Exception e) {
            Session.logout();
            e.printStackTrace();
            showError("No se pudo abrir el inventario.");
        }
    }

    private void showError(String message) {
        ErrorLabel.setText(message);
        ErrorLabel.setVisible(true);
        ErrorLabel.setManaged(true);
    }
}
