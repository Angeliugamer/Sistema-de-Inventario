package Controllers;

import Services.Session;
import Services.UserService;
import Services.ButtonSound;

import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

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

        UsserField.setOnAction(event -> AddresField.requestFocus());
        AddresField.setOnAction(event -> register());
    }

    // ANIMACIONES (porfavor no lo quiten)
    private void animatePicos() {
        // Superior: derecha a izquierda
        TranslateTransition superior =
                new TranslateTransition(
                        Duration.seconds(30),
                        picosSup
                );

        superior.setByX(-1084);
        superior.setInterpolator(Interpolator.LINEAR);
        superior.setCycleCount(Animation.INDEFINITE);
        superior.setAutoReverse(false);

        // Inferior: izquierda a derecha
        TranslateTransition inferior =
                new TranslateTransition(
                        Duration.seconds(30),
                        picosInf
                );

        inferior.setByX(1084);
        inferior.setInterpolator(Interpolator.LINEAR);
        inferior.setCycleCount(Animation.INDEFINITE);
        inferior.setAutoReverse(false);

        superior.play();
        inferior.play();
    }

    // MOSTRAR / OCULTAR CONTRASEÑA
    @FXML
    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            // Mostrar contraseña
            PasswordVisibleField.setText(AddresField.getText());

            AddresField.setVisible(false);
            AddresField.setManaged(false);

            PasswordVisibleField.setVisible(true);
            PasswordVisibleField.setManaged(true);

            EyeClosed.setVisible(false);
            EyeClosed.setManaged(false);

            EyeOpen.setVisible(true);
            EyeOpen.setManaged(true);

            PasswordVisibleField.requestFocus();
            PasswordVisibleField.positionCaret(
                    PasswordVisibleField.getText().length()
            );

        } else {
            // Ocultar contraseña
            AddresField.setText(PasswordVisibleField.getText());

            PasswordVisibleField.setVisible(false);
            PasswordVisibleField.setManaged(false);

            AddresField.setVisible(true);
            AddresField.setManaged(true);

            EyeOpen.setVisible(false);
            EyeOpen.setManaged(false);

            EyeClosed.setVisible(true);
            EyeClosed.setManaged(true);

            AddresField.requestFocus();
            AddresField.positionCaret(
                    AddresField.getText().length()
            );
        }
    }

    // REGISTRAR USUARIO
    @FXML
    private void register() {
        ButtonSound.playClick();

        if (!Session.isAdmin()) {
            showResult(
                    Alert.AlertType.ERROR,
                    "No se pudo completar el registro",
                    "No tienes autorización para registrar usuarios.",
                    2
            );
            return;
        }

        String username = UsserField.getText().trim();

        String password = passwordVisible
                ? PasswordVisibleField.getText()
                : AddresField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showResult(
                    Alert.AlertType.ERROR,
                    "No se pudo completar el registro",
                    "Completa todos los campos.",
                    1
            );
            return;
        }

        if (password.length() < 6) {
            showResult(
                    Alert.AlertType.ERROR,
                    "No se pudo completar el registro",
                    "La contraseña debe tener al menos 6 caracteres.",
                    0
            );
            return;
        }

        boolean registered = UserService.registerSeller(
                username, password
        );

        if (registered) {
            showResult(
                    Alert.AlertType.INFORMATION,
                    "Registro completado",
                    "El vendedor se registró correctamente.",
                    3
            );

            UsserField.clear();
            AddresField.clear();
            PasswordVisibleField.clear();

        } else {
            showResult(
                    Alert.AlertType.ERROR,
                    "No se pudo completar el registro",
                    "El usuario ya existe o ocurrió un error.",
                    2
            );
        }
    }

    // VENTANA INDEPENDIENTE DE RESULTADO
    private Timeline messageTimer;

    private void showResult(Alert.AlertType type,
            String title, String message, int errorType) {
        Alert alert = new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Estilo del mensaje.
        alert.getDialogPane().setStyle(
                "-fx-font-weight: bold;" + "-fx-text-fill: #12345A;"
        );

        // Seleccionar el sonido.
        switch (errorType) {
            case -1 -> ButtonSound.playClick("glitch");
            case 0  -> ButtonSound.playClick("Win11Error");
            case 1  -> ButtonSound.playClick("deny");
            case 2  -> ButtonSound.playClick("error");
            case 3  -> ButtonSound.playClick("KeyboardClick");
            default -> ButtonSound.playClick();
        }

        // Mostrar la alerta.
        alert.show();

        // Reiniciar el temporizador.
        if (messageTimer != null) {
            messageTimer.stop();
        }

        messageTimer = new Timeline(
                new KeyFrame(Duration.seconds(4), event -> alert.close())
        );

        messageTimer.play();
    }

    // REGRESAR AL LOGIN
    @FXML
    private void backToLogin() {
        try {
            AppNavigator.showInventory();

        } catch (Exception e) {
            e.printStackTrace();

            showResult(
                    Alert.AlertType.ERROR,
                    "Error",
                    "No se pudo abrir el Login.",
                    -1
            );
        }
    }
}
