package Controllers;

import Services.UserService;

import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.Optional;

public class RegisterController {
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
                (obs, oldValue, newValue) ->
                        PasswordVisibleField.setText(newValue)
        );

        PasswordVisibleField.textProperty().addListener(
                (obs, oldValue, newValue) -> {
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

    // REGISTRAR USUARIO

    @FXML
    private void register() {
        String username = UsserField.getText().trim();

        String password = passwordVisible
                ? PasswordVisibleField.getText()
                : AddresField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Completa todos los campos.");
            return;
        }

        if (password.length() < 6) {
            showError("La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        boolean registered = UserService.register(
                username, password
        );

        if (registered) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            alert.setTitle("Registro");
            alert.setHeaderText("¡Cuenta creada!");
            alert.setContentText("El usuario se registró correctamente.");

            alert.showAndWait();

            try {
                AppNavigator.showLogin();
            } catch (Exception e) {
                e.printStackTrace();
                showError("No se pudo regresar al Login.");
            }

        } else {
            showError(
                    "El usuario ya existe o ocurrió un error."
            );
        }
    }

    // REGRESAR AL LOGIN

    @FXML
    private void backToLogin() {
        try {
            AppNavigator.showLogin();

        } catch (Exception e) {
            e.printStackTrace();
            showError("No se pudo abrir el Login.");
        }
    }

    private void showError(String message) {

        ErrorLabel.setText(message);
        ErrorLabel.setVisible(true);
        ErrorLabel.setManaged(true);
    }
}
