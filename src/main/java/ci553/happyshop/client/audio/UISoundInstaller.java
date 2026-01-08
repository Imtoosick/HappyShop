package ci553.happyshop.client.audio;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.input.KeyEvent;

public final class UISoundInstaller {

    private UISoundInstaller() {}

    public static void install(Parent root) {
        //This is so all the buttons use the clicks
        root.lookupAll(".button").forEach(node -> {
            if (node instanceof Button button) {
                button.addEventHandler(
                        javafx.event.ActionEvent.ACTION,
                        e -> UISound.click()
                );
            }
        });

        // This is to add the click sound to the keyboard presses
        root.addEventFilter(KeyEvent.KEY_PRESSED, e -> UISound.click());
    }
}

