package ci553.happyshop.client.audio;

import javafx.scene.media.AudioClip;// made for short sounds

public final class UISound {

    private static final AudioClip CLICK =
            new AudioClip(UISound.class.getResource("/audio/click.mp3").toExternalForm());// .wav would have been better but couldn't find a suitable one

    private UISound() {}

    public static void click() {
        CLICK.play(0.75); // volume adjuster for the click
    }
}

