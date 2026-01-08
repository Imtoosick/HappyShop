package ci553.happyshop.client.orderTracker;

import ci553.happyshop.orderManagement.OrderHub;
import ci553.happyshop.orderManagement.OrderState;
import ci553.happyshop.utility.UIStyle;
import ci553.happyshop.utility.WinPosManager;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ci553.happyshop.client.audio.UISoundInstaller;


import java.util.Map;
import java.util.TreeMap;

/**
 * OrderTracker class is for tracking orders and their states.
 * It displays an ordersMap(a list of orders with their associated states) in a TextArea.
 * The ordersMap data is received from the OrderHub.
 */

public class OrderTracker {
    private final int WIDTH = UIStyle.trackerWinWidth;
    private final int HEIGHT = UIStyle.trackerWinHeight;

    // TreeMap (orderID,state) holding order IDs and their corresponding states.
    private static final TreeMap<Integer, OrderState> ordersMap = new TreeMap<>();
    private final TextArea taDisplay; //area to show all orderId and their state on the GUI

     //Constructor initializes the UI, a title Label, and a TextArea for displaying the order details.
    public OrderTracker() {
        Label laTitle = new Label("Order_ID,  State");
        laTitle.setStyle(UIStyle.labelTitleStyle);

        taDisplay = new TextArea();
        taDisplay.setEditable(false);
        taDisplay.setStyle(UIStyle.textFiledStyle);

        VBox vbox = new VBox(10,laTitle, taDisplay);
        vbox.setAlignment(Pos.TOP_CENTER);
        vbox.setStyle(UIStyle. rootStyleGray);

        Scene scene = new Scene(vbox, WIDTH, HEIGHT);
        UISoundInstaller.install(vbox);
        Stage window = new Stage();
        window.setScene(scene);
        window.setTitle("🛒Order Tracker");

        // Registers the window's position with WinPosManager.
        WinPosManager.registerWindow(window,WIDTH,HEIGHT); //calculate position x and y for this window
        window.show();
    }

    /**
     * Registers this OrderTracker instance with the OrderHub.
     * This allows the OrderTracker to receive updates on order state changes.
     */
    public void registerWithOrderHub(){
        OrderHub orderHub = OrderHub.getOrderHub();
        orderHub.registerOrderTracker(this);
    }

    /**
     * Sets the order map with new data and refreshes the display.
     * This method is called by OrderHub when order states are updated.
     */
    public void setOrderMap(TreeMap<Integer, OrderState> om) {
        ordersMap.clear(); // Clears the current map to replace it with the new data.
        ordersMap.putAll(om);// Adds all new order data to the map.
        displayOrderMap();// Updates the display with the new order map.
    }

     //Displays the current order map in the TextArea.
     //Iterates over the ordersMap and formats each order ID and state for display.
    private void displayOrderMap() {
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<Integer, OrderState> entry : ordersMap.entrySet()) {
            int orderId = entry.getKey();
            OrderState orderState = entry.getValue();
            sb.append(orderId).append(" ".repeat(5)).append(orderState).append("\n");
        }
        String textDisplay = sb.toString();
        taDisplay.setText(textDisplay);
    }

}
