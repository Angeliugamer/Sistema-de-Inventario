import Controllers.AppNavigator;
// import Services.UserService;
import Services.ButtonSound;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Estos son Los Comandos de las funciones

        // Registrar Administrador Nuevo
        // boolean created = UserService.createAdminByCode(
        //         "NOMBRE_ADMIN",
        //         "CONTRASENA_ADMIN"
        // );

        // System.out.println(created
        //         ? "Administrador creado."
        //         : "No se pudo crear el administrador."
        // );

        AppNavigator.setStage(stage);

        stage.setTitle("Sistema Inventario");
        stage.setResizable(false);

        AppNavigator.showLogin();

        stage.show();

        ButtonSound.playStartupSound();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
