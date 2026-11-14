package Controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AppNavigator {
    private static Stage primaryStage;

    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    public static void showLogin() throws IOException {
        loadScene("/Scenes/Login.fxml", 1280, 720);
    }

    public static void showRegister() throws IOException {
        loadScene("/Scenes/Register.fxml", 1280, 720);
    }

    public static void showInventory() throws IOException {
        loadScene("/Scenes/ListProduct.fxml", 1280, 720);
    }

    public static void showVenta() throws IOException {
        loadScene("/Scenes/Venta.fxml", 1280, 720);
    }

    private static void loadScene(
            String path, double width, double height)
            throws IOException {

        Parent root = FXMLLoader.load(
                AppNavigator.class.getResource(path)
        );

        primaryStage.setScene(
                new Scene(root, width, height)
        );
    }
}
