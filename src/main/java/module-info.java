module ci553.happyshop {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;
    requires javafx.media;


    opens ci553.happyshop to javafx.fxml;
    opens ci553.happyshop.client to javafx.fxml;
    opens ci553.happyshop.client.customer;
    opens ci553.happyshop.client.picker;
    opens ci553.happyshop.client.orderTracker;
    opens ci553.happyshop.client.warehouse;
    opens ci553.happyshop.client.emergency;

    exports ci553.happyshop;
    exports ci553.happyshop.client;
    exports ci553.happyshop.utility;
    exports ci553.happyshop.client.customer;
    exports ci553.happyshop.client.orderTracker;
    exports ci553.happyshop.client.emergency;
    exports ci553.happyshop.systemSetup;

}