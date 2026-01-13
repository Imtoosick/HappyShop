package ci553.happyshop.payment;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PaymentDialogue {

    public PaymentResult showAndWait(double totalAmount) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Payment");

        final PaymentResult[] result = new PaymentResult[]{
                PaymentResult.fail("Payment cancelled")
        };

        stage.setOnCloseRequest(e ->
                result[0] = PaymentResult.fail("Payment cancelled")
        );

        Scene paymentScene = buildPaymentScene(stage, totalAmount, result);
        stage.setScene(paymentScene);
        stage.showAndWait();

        return result[0];
    }

    private Scene buildPaymentScene(Stage stage, double totalAmount, PaymentResult[] resultHolder) {

        Label title = new Label("Pay Total: £" + String.format("%.2f", totalAmount));
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ToggleGroup group = new ToggleGroup();
        RadioButton rbCash = new RadioButton("Cash");
        RadioButton rbCard = new RadioButton("Card");
        rbCash.setToggleGroup(group);
        rbCard.setToggleGroup(group);
        rbCash.setSelected(true);

        HBox methodBox = new HBox(15, rbCash, rbCard);
        methodBox.setAlignment(Pos.CENTER);

        TextField tfCash = new TextField();
        tfCash.setPromptText("Cash given (e.g. 20)");
        Label lbChange = new Label("Change: £0.00");

        VBox cashBox = new VBox(8,
                new Label("Cash given:"),
                tfCash,
                lbChange
        );

        TextField tfCard = new TextField();
        tfCard.setPromptText("Card number (16 digits)");
        TextField tfExpiry = new TextField();
        tfExpiry.setPromptText("MM/YY");
        PasswordField pfCvv = new PasswordField();
        pfCvv.setPromptText("CVV");

        VBox cardBox = new VBox(8,
                new Label("Card number:"),
                tfCard,
                new Label("Expiry:"),
                tfExpiry,
                new Label("CVV:"),
                pfCvv
        );
        cardBox.setDisable(true);

        group.selectedToggleProperty().addListener((obs, o, n) -> {
            boolean isCash = rbCash.isSelected();
            cashBox.setDisable(!isCash);
            cardBox.setDisable(isCash);
        });

        Label msg = new Label();
        msg.setStyle("-fx-text-fill: red;");

        Button btnPay = new Button("Pay");
        Button btnCancel = new Button("Cancel");

        btnCancel.setOnAction(e -> {
            resultHolder[0] = PaymentResult.fail("Payment cancelled");
            stage.close();
        });

        btnPay.setOnAction(e -> {
            PaymentResult pr;

            if (rbCash.isSelected()) {
                pr = handleCash(totalAmount, tfCash.getText(), lbChange);
            } else {
                pr = validateCard(tfCard.getText(), tfExpiry.getText(), pfCvv.getText());
            }

            if (pr == null || !pr.isSuccess()) {
                msg.setText(pr == null ? "Payment cancelled" : pr.getMessage());
                return;
            }

            resultHolder[0] = pr;
            stage.setScene(buildSuccessScene(stage, pr));
        });

        HBox buttons = new HBox(10, btnPay, btnCancel);
        buttons.setAlignment(Pos.CENTER);

        VBox root = new VBox(12,
                title,
                methodBox,
                cashBox,
                cardBox,
                msg,
                buttons
        );
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.TOP_CENTER);

        return new Scene(root, 380, 420);
    }

    private Scene buildSuccessScene(Stage stage, PaymentResult pr) {

        Label title = new Label("Payment Successful");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label thanks = new Label("Thank you for shopping at Z Mart");

        Label info = new Label();
        if (pr.getMethod() == PaymentMethod.CASH) {
            info.setText("Change: £" + String.format("%.2f", pr.getChange()));
        } else {
            info.setText("Card: **** **** **** " + pr.getCardLast4());
        }

        Button btnClose = new Button("Close");
        btnClose.setOnAction(e -> stage.close());

        VBox root = new VBox(15, title, thanks, info, btnClose);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        return new Scene(root, 360, 220);
    }

    private PaymentResult handleCash(double total, String cashText, Label lbChange) {
        PaymentResult pr = validateCash(total, cashText);
        if (pr.isSuccess()) {
            lbChange.setText("Change: £" + String.format("%.2f", pr.getChange()));
        }
        return pr;
    }

    public PaymentResult validateCash(double total, String cashText) {
        double cash;
        try {
            cash = Double.parseDouble(cashText.trim());
        } catch (Exception e) {
            return PaymentResult.fail("Enter a valid cash amount.");
        }

        if (cash < total) {
            return PaymentResult.fail("Not enough cash. Need £" + String.format("%.2f", total));
        }

        double change = cash - total;
        return PaymentResult.okCash(cash, change);
    }

    public PaymentResult validateCard(String number, String expiry, String cvv) {
        String digits = number.replaceAll("\\s+", "");

        if (!digits.matches("\\d{16}"))
            return PaymentResult.fail("Card number must be 16 digits.");
        if (!expiry.matches("\\d{2}/\\d{2}"))
            return PaymentResult.fail("Expiry must be MM/YY.");
        if (!cvv.matches("\\d{3}"))
            return PaymentResult.fail("CVV must be 3 digits.");

        String last4 = digits.substring(12);
        return PaymentResult.okCard(0, last4);
    }
}

