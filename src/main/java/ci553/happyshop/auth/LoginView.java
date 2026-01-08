package ci553.happyshop.auth;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ci553.happyshop.client.audio.UISoundInstaller;

public class LoginView {

    private final AuthService authService;

    public LoginView(AuthService authService) {
        this.authService = authService;
    }

    public void show(Runnable onSuccess) {
        Stage stage = new Stage();

        stage.setTitle("HappyShop Login");// title of login screen

        TextField tfUser = new TextField();
        tfUser.setPromptText("Username");// prompt for username

        PasswordField pfPass = new PasswordField();
        pfPass.setPromptText("Password");//prompt for password

        Label msg = new Label();

        Button btnLogin = new Button("Login");
        btnLogin.setOnAction(e -> {
            if (authService.authenticate(tfUser.getText(), pfPass.getText())) {
                stage.close();
                onSuccess.run();
            } else {
                msg.setText("Invalid username or password");//Displays this message if the login is invalid
            }
        });

        VBox root = new VBox(10, tfUser, pfPass, btnLogin, msg);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.CENTER);
        UISoundInstaller.install(root);

        stage.setScene(new Scene(root, 300, 200));// this is where u can set the size of tthe login tab
        stage.show();
    }
}

