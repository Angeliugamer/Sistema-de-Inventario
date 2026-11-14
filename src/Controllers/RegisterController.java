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

    // REGISTRAR USUARIO

    @FXML
    private void register() {
        TextInputDialog usernameDialog = new TextInputDialog();

        usernameDialog.setTitle("Registro");
        usernameDialog.setHeaderText("Crear una cuenta");
        usernameDialog.setContentText("Nombre de usuario:");

        Optional<String> username = usernameDialog.showAndWait();

        if (username.isEmpty() || username.get().isBlank()) {
            return;
        }

        Dialog<String> passwordDialog = new Dialog<>();

        passwordDialog.setTitle("Registro");
        passwordDialog.setHeaderText("Introduce una contraseña");

        PasswordField passwordInput = new PasswordField();
        passwordInput.setPromptText("Contraseña");

        passwordDialog.getDialogPane().setContent(
                passwordInput
        );

        passwordDialog.getDialogPane()
                .getButtonTypes()
                .addAll(ButtonType.OK, ButtonType.CANCEL);

        passwordDialog.setResultConverter(button ->
                button == ButtonType.OK
                        ? passwordInput.getText()
                        : null
        );

        Optional<String> password = passwordDialog.showAndWait();

        if (password.isEmpty() || password.get().isBlank()) {
            return;
        }

        boolean registered = UserService.register(
                username.get(),
                password.get()
        );

        if (registered) {
            new Alert(Alert.AlertType.INFORMATION,
                    "Usuario registrado correctamente.").showAndWait();
            UsserField.setText(username.get());

        } else {
            showError(
                    "El usuario ya existe o los datos no son válidos."
            );
        }
    }

    private void showError(String message) {
        ErrorLabel.setText(message);
        ErrorLabel.setVisible(true);
        ErrorLabel.setManaged(true);
    }
}
