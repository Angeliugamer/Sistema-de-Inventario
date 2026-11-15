package Services;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

public class ButtonSound {
    private static MediaPlayer player;

    public static void playClick() {
        try {
            URL sound = ButtonSound.class.getResource("/Sonidos/Win11%20notificacion.mp3");

            if (sound == null) {
                System.out.println("No se encontró el sonido.");
                return;
            }

            if (player != null) {
                player.stop();
                player.dispose();
            }

            Media media = new Media(sound.toExternalForm());
            player = new MediaPlayer(media);
            player.play();

        } catch (Exception e) {
            System.out.println("Error al reproducir el sonido: " + e.getMessage());
        }
    }
}
