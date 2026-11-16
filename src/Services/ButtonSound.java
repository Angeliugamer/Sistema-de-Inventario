package Services;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

public class ButtonSound {
    private static final String DEFAULT_SOUND = "Win11 notificacion.mp3";
    private static MediaPlayer player;

    // REPRODUCIR SONIDO PREDETERMINADO
    public static void playClick() {
        playClick(DEFAULT_SOUND);
    }

    // REPRODUCIR SONIDO PERSONALIZADO
    public static void playClick(String soundName) {
        try {
            if (soundName == null || soundName.isBlank()) {
                soundName = DEFAULT_SOUND;
            }

            // Agregar extensión si no está especificada
            if (!soundName.toLowerCase().endsWith(".mp3")) {
                soundName += ".mp3";
            }

            // Buscar el sonido en Resources
            URL sound = ButtonSound.class.getResource(
                    "/" + soundName
            );

            if (sound == null) {
                System.out.println(
                        "No se encontró el sonido: " + soundName
                );
                return;
            }

            // Detener el sonido anterior
            if (player != null) {
                player.stop();
                player.dispose();
            }

            // Crear y reproducir el nuevo sonido
            Media media = new Media(sound.toExternalForm());

            player = new MediaPlayer(media);

            player.setOnError(() ->
                    System.out.println(
                            "Error de reproducción: " + player.getError())
            );

            player.play();

        } catch (Exception e) {
            System.out.println(
                    "Error al reproducir sonido: " + e.getMessage()
            );
        }
    }
}
