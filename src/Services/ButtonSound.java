package Services;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

public class ButtonSound {
    private static final String DEFAULT_SOUND = "Win11Not.mp3";
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

    public static void playStartupSound() {
        int build = getWindowsBuild();

        String sound;

        if (build >= 22000) {
            // Windows 11 y futuras versiones
            sound = "Windows 11 Startup sound";
        } else if (build >= 10240) {
            // Windows 10
            sound = "Windows 10 Startup sound";
        } else if (build > 0) {
            // Windows anteriores a Windows 10
            sound = "Windows XP Startup Sound";
        } else {
            // Si no se pudo identificar el sistema
            sound = "Windows 10 Startup sound";
        }

        playClick(sound);
    }

    private static int getWindowsBuild() {
        try {
            Process process = new ProcessBuilder(
                    "reg.exe", "query",
                    "HKLM\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion",
                    "/v", "CurrentBuildNumber"
            ).redirectErrorStream(true).start();

            if (!process.waitFor(2, java.util.concurrent.TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return -1;
            }

            String output = new String(
                    process.getInputStream().readAllBytes()
            );

            java.util.regex.Matcher matcher =
                    java.util.regex.Pattern
                            .compile("CurrentBuildNumber\\s+REG_SZ\\s+(\\d+)")
                            .matcher(output);

            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }

        } catch (Exception e) {
            System.out.println("No se pudo identificar la versión de Windows.");
        }

        return -1;
    }
}
