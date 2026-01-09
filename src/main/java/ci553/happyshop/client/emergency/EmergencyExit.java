package ci553.happyshop.client.emergency;

import ci553.happyshop.utility.UIStyle;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import ci553.happyshop.client.audio.UISoundInstaller;

public class EmergencyExit {
    private final int WIDTH = UIStyle.EmergencyExitWinWidth;
    private final int HEIGHT = UIStyle.EmergencyExitWinHeight;
    private static EmergencyExit emergencyExit;

    private BorderPane root;

    public static EmergencyExit getEmergencyExit() {
        if (emergencyExit == null)
            emergencyExit = new EmergencyExit();
        return emergencyExit;
    }

    private EmergencyExit() {
        ImageView ivExit = new ImageView("ShutDown.jpg");
        ivExit.setFitWidth(WIDTH - 100);
        ivExit.setFitHeight(WIDTH - 100);
        ivExit.setPreserveRatio(true);

        Button btnExit = new Button();
        btnExit.setGraphic(ivExit);
        btnExit.setOnAction(event -> {
            Platform.exit();
            System.exit(0);
        });

        root = new BorderPane();
        root.setCenter(btnExit);
        root.setStyle(UIStyle.rootStyle);
        UISoundInstaller.install(root);
    }

    public Parent getRoot() {
        return root;
    }
}
