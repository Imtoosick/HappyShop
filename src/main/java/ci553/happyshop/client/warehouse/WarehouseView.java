package ci553.happyshop.client.warehouse;

import ci553.happyshop.catalogue.Product;
import ci553.happyshop.utility.StorageLocation;
import ci553.happyshop.utility.UIStyle;
import ci553.happyshop.utility.WinPosManager;
import ci553.happyshop.utility.WindowBounds;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import javafx.geometry.Insets;
import javafx.scene.input.MouseEvent;
import ci553.happyshop.client.audio.UISoundInstaller;

/**
 * Some emojis used in the UI. If the emoji does not work on your OS,
 * please change them to their unique Unicode codes.
 * 🔍 (Search): \uD83D\uDD0D
 * ➕ (Plus): \u2795
 * ➖ (Minus): \u2796
 * 🛒 (Shopping Cart): \uD83D\uDED2
 * 🏬 (Department Store): \uD83C\uDFEC
 *
 * eg Button btnSearch = new Button("\uD83D\uDD0D");
 *    Button btnSearch = new Button("🔍");
 *    case "\uD83D\uDD0D",
 *    case "🔍"
 */

/**
 * The Warehouse interface (WarehouseView) contains two main pages:
 * a divider line is between the two pages
 * 1. Search Page:
 *    - The key data model for the search page is an observable product list.
 *    - This list is updated by the model when searching the database.
 *    - A ListView observes the product list. Whenever the list changes,
 *      the ListView automatically updates itself based on the specified cell factory.
 *
 * 2. Product Form Page:
 *    - The form page contains a ComboBox for selecting between two actions:
 *      * Editing an existing product
 *      * Adding a new product to stock
 *    - Based on the ComboBox selection, one of two VBoxes will be shown:
 *      * EditProductVBox (for editing existing products), referred to as **EditChild** in the code
 *      * NewProductVBox (for adding new products), referred to as **NewProChild** in the code
 *    - Only one VBox (EditChild or NewProChild) is active and visible at a time, depending on the selected action.
 */

public class WarehouseView  {
    private final int WIDTH = UIStyle.warehouseWinWidth;
    private final int HEIGHT = UIStyle.warehouseWinHeight;
    private final int COLUMN_WIDTH = WIDTH / 2 - 10;

    public WarehouseController controller;
    private Stage viewWindow;
    /** A reference to the main window that is used to get its bounds (position and size).
     * This allows us to position other windows (like the History window or alert) relative to the Warehouse window.
     * It helps in keeping the UI layout consistent by placing new windows near the Warehouse window.
     */

    //some elements in searchPage
    TextField tfSearchKeyword; //user typing in it
    private Label laSearchSummary; //eg. the lable shows "3 products found" after search
    private ObservableList<Product> obeProductList; //observable product list
    ListView<Product> obrLvProducts; //A ListView observes the product list

    //ProductFormPage:has two children at a time,
    ComboBox<String> cbProductFormMode; //the first child
    private VBox vbEditProduct; //the seceond child
    private VBox vbNewProduct; //another second child
    String theProFormMode ="EDIT";
    /** productFormPage has two children at a time,
     * 1. cbProductFormMode: A ComboBox that holds two action types for the product form:
     *    - "EDIT": For editing an existing product
     *    - "NEW": For adding a new product to stock
     * The action mode (either "EDIT" or "NEW") is stored in the 'theProFormMode' variable to keep track of the current mode.
     *
     * The following two second childeren swap based on the selected value of the ComboBox:
     * 2. vbEditProduct: contains the UI elements for editing an existing product (visible when "EDIT" is selected)
     * 2. vbNewProduct: contains the UI elements for adding a new product to stock (visible when "NEW" is selected)
     */

    //some elements in vbEditProduct, we need to getValue from them and setValue for them
    private TextField tfIdEdit;
    TextField tfPriceEdit;
    TextField tfStockEdit;
    TextField tfChangeByEdit;
    TextArea taDescriptionEdit;
    private ImageView ivProEdit;
    String userSelectedImageUriEdit;
    boolean isUserSelectedImageEdit = false;
    /** userSelectedImageUriEdit: URI of the image selected by the user during editing.
     * This value is retrieved from the image chooser when the user selects or changes the image for an existing product.
     *
     * isUserSelectedImageEdit: A flag indicating if the user has selected a new image for editing an existing product.
     * This helps the model determine if the old image should be deleted and the new image copied to the destination folder.
     */
    private Button btnAdd;
    private Button btnSub;
    private Button btnCancelEdit;
    private Button btnSubmitEdit;
    /** Normally, buttons are not kept as instance variables. However, in this case,
     * btnAdd, btnSub, btnCancelEdit, and btnSubmitEdit:
     * They are kept as instance variables to manage their states (enabled/disabled) when necessary,
     * eg. when the Cancel or Submit buttons are clicked, to prevent unintended interactions.
     */

    //some elements in vbNewProduct,we need to getValue from them and setValue for them
    TextField tfIdNewPro;
    TextField tfPriceNewPro;
    TextField tfStockNewPro;
    TextArea taDescriptionNewPro;
    private ImageView ivProNewPro;
    String imageUriNewPro; //user slected image Uri
    // URI of the image selected by the user for a new product. This value is retrieved from the image chooser.

    public void start(Stage window) {
        VBox vbSearchPage = createSearchPage();
        VBox vbProductFormPage = createProductFormPage();

        // Divider line between SearchPage and ProductFormPage
        Line line = new Line(0, 0, 0, HEIGHT);
        line.setStrokeWidth(4);
        line.setStroke(Color.LIGHTGREEN);
        VBox lineContainer = new VBox(line);
        lineContainer.setPrefWidth(4);
        lineContainer.setAlignment(Pos.CENTER);

        //top level layout manager
        HBox hbRoot = new HBox(15, vbSearchPage, lineContainer, vbProductFormPage);
        hbRoot.setStyle(UIStyle.rootStyleWarehouse);

        Scene scene = new Scene(hbRoot, WIDTH, HEIGHT);
        UISoundInstaller.install(hbRoot);
        window.setScene(scene);
        window.setTitle("Search_Page  🛒🛒HappyShop_Warehouse🛒🛒  ProductForm_Page(Edit & AddNew Product)");
        WinPosManager.registerWindow(window,WIDTH,HEIGHT); // Registers the window with WinPosManager to
        // dynamically position itself based on its size, and any already displayed windows.
        window.show();
        viewWindow = window; // Sets the global viewWindow reference to this window for future reference and management.
    }

    private VBox createSearchPage() {
        Label laTitle = new Label("Search by product ID/Name");
        laTitle.setStyle(UIStyle.labelTitleStyle);

        tfSearchKeyword = new TextField();
        tfSearchKeyword.setStyle(UIStyle.textFiledStyle);
        tfSearchKeyword.setOnAction(actionEvent -> {
            try {
                controller.process("🔍");  //pressing enter can also do search
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        Button btnSearch = new Button("🔍");
        //Button btnSearch = new Button("\uD83D\uDD0D"); // Unicode for 🔍
        btnSearch.setOnAction(this::buttonClick);
        btnSearch.setStyle(UIStyle.buttonStyle);
        HBox hbSearch = new HBox(10, tfSearchKeyword, btnSearch);
        hbSearch.setAlignment(Pos.CENTER);

        laSearchSummary = new Label("Search Summary");
        laSearchSummary.setStyle(UIStyle.labelStyle);
        Button btnEdit = new Button("Edit");
        btnEdit.setStyle(UIStyle.greenFillBtnStyle);
        btnEdit.setOnAction(this::buttonClick);

        Button btnDelete = new Button("Delete");
        btnDelete.setStyle(UIStyle.grayFillBtnStyle);
        btnDelete.setOnAction(this::buttonClick);

        HBox hbLaBtns = new HBox(10, laSearchSummary, btnEdit,btnDelete);
        hbLaBtns.setAlignment(Pos.CENTER);
        hbLaBtns.setPadding(new Insets(5)); //setPadding only works on Layout manager
        //hbLaBtns.setStyle("-fx-padding: 5px;"); //setStyle works on any Node (eg. layout manager, controls)

        // data, an observable ArrayList, observed by obrLvProducts
        obeProductList = FXCollections.observableArrayList();
        obrLvProducts = new ListView<>(obeProductList);//ListView proListView observes proList
        obrLvProducts.setPrefHeight(HEIGHT - 100);
        obrLvProducts.setFixedCellSize(50);
        obrLvProducts.setStyle(UIStyle.listViewStyle);

        VBox vbSearchResult = new VBox(5,hbLaBtns, obrLvProducts);

        /**
         * When is setCellFactory() Needed?
         * If you want to customize each row’s content (e.g.,images, buttons, labels, etc.).
         * If you need special formatting (like colors or borders).
         *
         * When is setCellFactory() NOT Needed?
         * Each row is just plain text without images or formatting.
         */
        obrLvProducts.setCellFactory(param -> new ListCell<Product>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);

                if (empty || product == null) {
                    setGraphic(null);
                    System.out.println("setCellFactory - empty item");
                } else {
                    String imageName = product.getProductImageName(); // Get image name (e.g. "0001.jpg")
                    String relativeImageUrl = StorageLocation.imageFolder + imageName;
                    // Get the full absolute path to the image
                    Path imageFullPath = Paths.get(relativeImageUrl).toAbsolutePath();
                    String imageFullUri = imageFullPath.toUri().toString();// Build the full image Uri

                    ImageView ivPro;
                    try {
                        ivPro = new ImageView(new Image(imageFullUri, 50,45, true,true)); // Attempt to load the product image
                    } catch (Exception e) {
                        // If loading fails, use a default image directly from the resources folder
                        ivPro = new ImageView(new Image("imageHolder.jpg",50,45,true,true)); // Directly load from resources
                    }

                    Label laProToString = new Label(product.toString()); // Create a label for product details
                    HBox hbox = new HBox(10, ivPro, laProToString); // Put ImageView and label in a horizontal layout
                    setGraphic(hbox);  // Set the whole row content
                }
            }
        });

        VBox vbSearchPage = new VBox(10, laTitle, hbSearch, vbSearchResult);

        vbSearchPage.setPrefWidth(COLUMN_WIDTH-10);
        vbSearchPage.setAlignment(Pos.TOP_CENTER);

        return vbSearchPage;

        /** NOTE for make image
         * user selected image at runtime, like with a FileChooser, you cannot use getResource().
         * getResource() is only for static files already bundled inside app.
         * User-selected files are real files on the computer, not inside the app resources.
         */
    }

    private VBox createProductFormPage() {
        cbProductFormMode = new ComboBox<>();
        cbProductFormMode.setStyle(UIStyle.comboBoxStyle);
        cbProductFormMode.getItems().addAll("Edit Existing Product in Stock", "Add New Product to Stock");
        // Set default selected value, so only when value changed trigger setOnAction
        cbProductFormMode.setValue("Edit Existing Product in Stock");

        vbEditProduct = createEditProdcutChild();
        disableEditProductChild(true); //disable editable component until user selects a product and cilck btnEdit
        vbNewProduct = createNewProductChild();

        // Initially set the second child (after ComboBox) to editProduct
        VBox vbProductFormPage = new VBox(10, cbProductFormMode, vbEditProduct);

        // Check selected value and place the corerect child
        //isImageNameEditable for imageChooser using a single method to differciate from edit/add product
        cbProductFormMode.setOnAction(actionEvent -> {
            if (cbProductFormMode.getValue().equals("Edit Existing Product in Stock")) {
                vbProductFormPage.getChildren().set(1,vbEditProduct);
                theProFormMode = "EDIT";
            }
            if (cbProductFormMode.getValue().equals("Add New Product to Stock")) {
                vbProductFormPage.getChildren().set(1,vbNewProduct);
                theProFormMode = "NEW";
            }
        });

        vbProductFormPage.setPrefWidth(COLUMN_WIDTH+20);
        vbProductFormPage.setAlignment(Pos.TOP_CENTER);
        return vbProductFormPage;
    }


    private VBox createEditProdcutChild() {
        //HBox for Id Label and TextField
        Label laId = new Label("ID"+" ".repeat(8));
        laId.setStyle(UIStyle.labelStyle);
        tfIdEdit = new TextField();
        tfIdEdit.setEditable(false);
        tfIdEdit.setStyle("-fx-font-size: 14px; -fx-pref-width: 100px;");
        HBox hbId = new HBox(10, laId, tfIdEdit);
        hbId.setAlignment(Pos.CENTER_LEFT);

        // HBox for Price Label and TextField
        Label laPrice = new Label("Price_£");
        laPrice.setStyle(UIStyle.labelStyle);
        tfPriceEdit = new TextField();
        tfPriceEdit.setStyle("-fx-font-size: 14px; -fx-pref-width: 100px;");
        HBox hbPrice = new HBox(10, laPrice, tfPriceEdit);
        hbPrice.setAlignment(Pos.CENTER_LEFT);

        //VBox for id and price
        VBox vbIdPrice = new VBox(10, hbId, hbPrice);

        // Product Image
        ivProEdit = new ImageView("WarehouseImageHolder.jpg");
        ivProEdit.setFitWidth(100);
        ivProEdit.setFitHeight(70);
        ivProEdit.setPreserveRatio(true); //Image keeps its original shape and fits inside 100×70
        ivProEdit.setSmooth(true);//make it smooth and nice-looking

        // Image Click Event (Open File Chooser)
        ivProEdit.setOnMouseClicked(this::imageChooser);

        // HBox for Id, Price, and Image in one row
        HBox hbIdPriceImage = new HBox(20, vbIdPrice, ivProEdit);
        hbIdPriceImage.setAlignment(Pos.CENTER_LEFT);

        // Editing stock
        Label laStock = new Label("Stock"+" ".repeat(3));
        laStock.setStyle(UIStyle.labelStyle);

        // TextField current stock
        tfStockEdit = new TextField();
        tfStockEdit.setEditable(false);
        tfStockEdit.setStyle("-fx-font-size: 14px; -fx-pref-width: 70px;");

        // TextField Change By
        tfChangeByEdit = new TextField();
        tfChangeByEdit.setPromptText("change by");
        tfChangeByEdit.setStyle("-fx-font-size: 14px; -fx-pref-width: 50px;");

        // Add and Subtract Buttons for changing stock
        btnAdd = new Button("➕");
        btnAdd.setStyle(UIStyle.greenFillBtnStyle);
        btnAdd.setPrefWidth(35);
        btnAdd.setOnAction(this::buttonClick);

        btnSub = new Button("➖");
        btnSub.setStyle(UIStyle.redFillBtnStyle);
        btnSub.setPrefWidth(35);
        btnSub.setOnAction(this::buttonClick);

        //Hbox for all things related to edit stock
        HBox hbStock = new HBox(10, laStock, tfStockEdit,tfChangeByEdit, btnAdd,btnSub);
        hbStock.setAlignment(Pos.CENTER_LEFT);

        // VBox for Description label and TextArea
        Label laDes = new Label("Description:");
        laDes.setStyle(UIStyle.labelStyle);
        taDescriptionEdit = new TextArea();
        taDescriptionEdit.setPrefSize(COLUMN_WIDTH-20, 20);
        taDescriptionEdit.setWrapText(true);
        taDescriptionEdit.setStyle(UIStyle.textFiledStyle);
        VBox vbDescription = new VBox(laDes, taDescriptionEdit);
        vbDescription.setAlignment(Pos.CENTER_LEFT);

        // OK & Clear Buttons
        btnCancelEdit = new Button("Cancel");
        btnCancelEdit.setStyle(UIStyle.grayFillBtnStyle);
        btnCancelEdit.setPrefWidth(100);
        btnCancelEdit.setOnAction(this::buttonClick);

        btnSubmitEdit = new Button("Submit");
        btnSubmitEdit.setStyle(UIStyle.blueFillBtnStyle);
        btnSubmitEdit.setPrefWidth(100);
        btnSubmitEdit.setOnAction(this::buttonClick);

        // HBox for OK & Cancel Buttons
        HBox hbOkCancelBtns = new HBox(15, btnCancelEdit, btnSubmitEdit);
        hbOkCancelBtns.setAlignment(Pos.CENTER);
        //hbOkCancelBtns.setPadding(new Insets(5));

        // Main Layout
        VBox vbEditStockChild = new VBox(10, hbIdPriceImage, hbStock, vbDescription, hbOkCancelBtns);
        vbEditStockChild.setStyle(UIStyle.manageStockChildStyle);
        return vbEditStockChild;
    }


    private VBox createNewProductChild() {
        //HBox for Id Label and TextField
        Label laId = new Label("ID"+ " ".repeat(9));
        laId.setStyle(UIStyle.labelStyle);
        tfIdNewPro = new TextField();
        tfIdNewPro.setStyle("-fx-font-size: 14px; -fx-pref-width: 100px;");
        HBox hbId = new HBox(10, laId, tfIdNewPro);
        hbId.setAlignment(Pos.CENTER_LEFT);

        // HBox for Price Label and TextField
        Label laPrice = new Label("Price_£ ");
        laPrice.setStyle(UIStyle.labelStyle);
        tfPriceNewPro = new TextField();
        tfPriceNewPro.setStyle("-fx-font-size: 14px; -fx-pref-width: 100px;");
        HBox hbPrice = new HBox(10, laPrice, tfPriceNewPro);
        hbPrice.setAlignment(Pos.CENTER_LEFT);

        //  HBox for stock label and textFiled
        Label laStock = new Label("Stock" +" ".repeat(4));
        laStock.setStyle(UIStyle.labelStyle);
        tfStockNewPro = new TextField();
        tfStockNewPro.setStyle("-fx-font-size: 14px; -fx-pref-width: 100px;");
        HBox hbStock = new HBox(10, laStock, tfStockNewPro);
        hbStock.setAlignment(Pos.CENTER_LEFT);

        //VBox for id, price,stock
        VBox vbIdPriceStock = new VBox(10, hbId, hbPrice,hbStock);

        // VBox for Product Image and name keyword
        ivProNewPro = new ImageView("WarehouseImageHolder.jpg");
        ivProNewPro.setFitWidth(100);
        ivProNewPro.setFitHeight(70);
        ivProEdit.setPreserveRatio(true); //Image keeps its original shape and fits inside 100×70
        ivProEdit.setSmooth(true);//make it smooth and nice-looking

        // Image Click Event (Open File Chooser)
        ivProNewPro.setOnMouseClicked(this::imageChooser);
        //Hbox for id,price,stock,image
        HBox hbIdPriceStockImage = new HBox(20, vbIdPriceStock, ivProNewPro);
        hbIdPriceStockImage.setAlignment(Pos.CENTER_LEFT);

        // VBox for Description label and TextArea
        Label laDes = new Label("Description:");
        laDes.setStyle(UIStyle.labelStyle);
        taDescriptionNewPro = new TextArea();
        taDescriptionNewPro.setPrefSize(COLUMN_WIDTH-20, 20);
        taDescriptionNewPro.setWrapText(true);
        taDescriptionNewPro.setStyle(UIStyle.textFiledStyle);
        VBox vbDescription = new VBox(laDes, taDescriptionNewPro);
        vbDescription.setAlignment(Pos.CENTER_LEFT);

        // OK & Cancel Buttons
        Button btnClear = new Button("Cancel");
        btnClear.setStyle(UIStyle.grayFillBtnStyle);
        btnClear.setPrefWidth(100);
        btnClear.setOnAction(this::buttonClick);

        Button btnAddNewPro = new Button("Submit");
        btnAddNewPro.setStyle(UIStyle.blueFillBtnStyle);
        btnAddNewPro.setPrefWidth(100);
        btnAddNewPro.setOnAction(this::buttonClick);
        // HBox for OK & clear Buttons
        HBox hbOkCancelBtns = new HBox(15, btnClear, btnAddNewPro);
        hbOkCancelBtns.setAlignment(Pos.CENTER);
        //hbOkCancelBtns.setPadding(new Insets(5));

        // Main Layout
        VBox vbAddNewProductToStockChild = new VBox(10, hbIdPriceStockImage, vbDescription, hbOkCancelBtns);
        vbAddNewProductToStockChild.setStyle(UIStyle.manageStockChildStyle1);
        return vbAddNewProductToStockChild;
    }

    //disable editable controls before user select a product and click the button edit
    private void disableEditProductChild(boolean disable) {
        tfPriceEdit.setDisable(disable);
        tfChangeByEdit.setDisable(disable);
        btnAdd.setDisable(disable);
        btnSub.setDisable(disable);
        ivProEdit.setDisable(disable);
        taDescriptionEdit.setDisable(disable);
        btnCancelEdit.setDisable(disable);
        btnSubmitEdit.setDisable(disable);
    }


    private void buttonClick(ActionEvent event)  {
        Button btn= (Button)event.getSource();
        String action = btn.getText();

        //only when user click btnEidt and a product was selected, enable editable field of editChild
        if(action.equals("Edit") && obrLvProducts.getSelectionModel().getSelectedItem()!=null) {
            disableEditProductChild(false); //a product was selected, enable editChild
            cbProductFormMode.setValue("Edit Existing Product in Stock"); //show EditChild
        }

        try{
            controller.process(action);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void imageChooser(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(null); //return absolute fullpath of the user selected file
                                                              //eg C:/Users/John/Pictures/sample.jpg
        if (file != null) {

            if (theProFormMode.equals("EDIT")) {
                isUserSelectedImageEdit = true;
                // Set image preview
                ivProEdit.setImage(new Image(file.toURI().toString()));
                // Get the selected image path, and name
                userSelectedImageUriEdit = file.getAbsolutePath(); //eg C:\Users\shan\Desktop\mark.jpg
                //file.getParent();  // Get the folder
                System.out.println("Selected Image Path: " + userSelectedImageUriEdit);
                System.out.println("Selected Image name: " + file.getName());
            }
            if (theProFormMode.equals("NEW")) {
                // Set image preview
                ivProNewPro.setImage(new Image(file.toURI().toString()));
                imageUriNewPro = file.getAbsolutePath();
                System.out.println("Selected Image Path: " + imageUriNewPro);
                System.out.println("Selected Image name: " + file.getName());
            }
        }
    }

    //update the product listVew of serachPage
    void updateObservableProductList( ArrayList<Product> productList) {
        int proCounter = productList.size();
        System.out.println(proCounter);
        laSearchSummary.setText(proCounter + " products found");
        laSearchSummary.setVisible(true);
        obeProductList.clear();
        obeProductList.addAll(productList);
    }

    void updateBtnAddSub(String stock){
        tfStockEdit.setText(stock);
        tfChangeByEdit.clear();
    }

    //update interface of editing existing product in stock
    void updateEditProductChild(String id, String price, String stock, String des, String imageUrl) {
        tfIdEdit.setText(id);
        tfPriceEdit.setText(price);
        tfStockEdit.setText(stock);
        taDescriptionEdit.setText(des);

        System.out.println(imageUrl);
        try{
            ivProEdit.setImage(new Image(imageUrl));  // Attempt to load the product image
        } catch (Exception e) {
            // If loading fails, use a default image directly from the resources folder
            ivProEdit.setImage(new Image("imageHolder.jpg"));
        }
    }

    void resetEditChild() {
        tfIdEdit.setText("");
        tfPriceEdit.setText("");
        tfStockEdit.setText("");
        tfChangeByEdit.setText("");
        taDescriptionEdit.setText("");
        ivProEdit.setImage(new Image("WarehouseImageHolder.jpg"));
        disableEditProductChild(true);
    }

    void resetNewProChild() {
       tfIdNewPro.setText("");
       tfPriceNewPro.setText("");
       tfStockNewPro.setText("");
       taDescriptionNewPro.setText("");
       ivProNewPro.setImage(new Image("WarehouseImageHolder.jpg"));
       imageUriNewPro = null; //clear the selcted image
       System.out.println("resetNewProChild in view called");
    }

    WindowBounds getWindowBounds() {
        return new WindowBounds(viewWindow.getX(),
                                viewWindow.getY(),
                                viewWindow.getWidth(),
                                viewWindow.getHeight());
    }

    //   //another way to reset the editChild and NewProChild
//   // remove the current one then recreate them and add them back
//    //not use it in this version
//    public void resetManageStockChild() {
//        vbManagePage.getChildren().remove(1); // Remove the second child (editChild or addNewProChild)
//
//        //Decide which child to recreate and add back
//        if (theManageType.equals("edit")) {
//            vbEditProChild = editStockChild(); // Recreate the child
//            vbManagePage.getChildren().add(vbEditProChild);
//            proListView.requestFocus();
//            imageSelectedEdit = false;//reset to false if the user canged image in previous editing
//        }
//        if (theManageType.equals("addNew")) {
//            vbAddProChild = addNewProductToStockChild();  // Recreate the child
//            vbManagePage.getChildren().add(vbAddProChild);
//            tfIdNewPro.requestFocus();
//            imageSelectedNewPro = false;
//        }
//    }

//

}


