package ci553.happyshop.client.audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

public final class BackgroundMusic {

    private static MediaPlayer player;

    private BackgroundMusic() {}

    public static void start() {
        if (player != null) return; //if its already playing

        URL music = BackgroundMusic.class.getResource("/audio/bgm.mp3");
        if (music == null) {
            throw new IllegalStateException("bgm.mp3 not found in /resources/audio");// if the audio file is not in the correct place
        }

        Media media = new Media(music.toExternalForm());
        player = new MediaPlayer(media);

        player.setCycleCount(MediaPlayer.INDEFINITE);
        player.setVolume(0.25);// Adjusts the volume level
        player.play();
    }

    public static void stop() {
        try {
            if (player != null) {
                player.stop();
                player.dispose();
                player = null;
            }
        } catch (Exception ignored) {
        }
    }

}

