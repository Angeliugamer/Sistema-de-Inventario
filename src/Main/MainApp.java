package Main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        showLogin();

        primaryStage.setTitle("Sistema Inventario");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void showLogin() throws IOException {
        Parent root = FXMLLoader.load(
                MainApp.class.getResource("/Scenes/Login.fxml")
        );

        primaryStage.setScene(new Scene(root, 1280, 720));
    }

    public static void showInventory() throws IOException {
        // CAMBIAR ESTA RUTA POR LA DE TU ESCENA PRINCIPAL

        Parent root = FXMLLoader.load(
                MainApp.class.getResource("/Scenes/Inventory.fxml")
        );

        primaryStage.setScene(new Scene(root));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
