import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Estas son Los Comandos de las funciones

        // Registrar
        // UserService.register("Angeliu", "MiContraseña");

        // Iniciar sesión
        // User user = UserService.authenticate("Angeliu", "MiContraseña");

        // Registrar una compra
        // PurchaseService.addPurchase("Teclado", 2, 150000);

        // Consultar las compras del usuario actual
        // List<Purchase> historial = PurchaseService.getUserPurchases();

        // for (Purchase compra : historial) {
        //     System.out.println(compra.getProduct());
        //     System.out.println(compra.getTotal());
        //     System.out.println(compra.getDate());
        // }

        // Aquí cargas tu interfaz FXML
    }

    public static void main(String[] args) {
        launch(args);
    }
}