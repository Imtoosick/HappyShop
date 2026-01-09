package ci553.happyshop.orderManagement;

import ci553.happyshop.catalogue.Order;
import ci553.happyshop.catalogue.Product;
import ci553.happyshop.client.orderTracker.OrderTracker;
import ci553.happyshop.client.picker.PickerModel;
import ci553.happyshop.storageAccess.OrderFileManager;
import ci553.happyshop.utility.StorageLocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * <p>{@code OrderHub} serves as the heart of the ordering system.
 * This class implements the Singleton pattern to ensure a single instance governs
 * all order-related logic across the system.</p>
 *
 * <p> It is the central coordinator responsible for managing all orders. It handles:
 *   Creating and tracking orders
 *   Maintaining and updating the internal order map, <OrderId, OrderState>
 *   Delegating file-related operations (e.g., updating state and moving files) to OrderFileManager class
 *   Loading orders in the "ordered" and "progressing" states from storage during system startup
 *
 * <p> OrderHub also follows the Observer pattern: it notifies registered observers such as OrderTracker
 * and PickerModel whenever the order data changes, keeping the UI and business logic in sync.</p>
 *
 * <p>As the heart of the ordering system, OrderHub connects customers, pickers, and tracker,
 * managementing logic into a unified workflow.</p>
 */

public class OrderHub  {
    private static OrderHub orderHub; //singleton instance

    private final Path orderedPath = StorageLocation.orderedPath;
    private final Path progressingPath = StorageLocation.progressingPath;
    private final Path collectedPath = StorageLocation.collectedPath;

    private TreeMap<Integer,OrderState> orderMap = new TreeMap<>();
    private TreeMap<Integer,OrderState> OrderedOrderMap = new TreeMap<>();
    private TreeMap<Integer,OrderState> progressingOrderMap = new TreeMap<>();

    /**
     * Two Lists to hold all registered OrderTracker and PickerModel observers.
     * These observers are notified whenever the orderMap is updated,
     * but each observer is only notified of the parts of the orderMap that are relevant to them.
     * - OrderTrackers will be notified of the full orderMap, including all orders (ordered, progressing, collected),
     *   but collected orders are shown for a limited time (10 seconds).
     * - PickerModels will be notified only of orders in the "ordered" or "progressing" states, filtering out collected orders.
     */
    private ArrayList<OrderTracker> orderTrackerList = new ArrayList<>();
    private ArrayList<PickerModel> pickerModelList = new ArrayList<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    //Singleton pattern
    private OrderHub() {}
    public static OrderHub getOrderHub() {
        if (orderHub == null)
            orderHub = new OrderHub();
            return orderHub;
    }

    //Creates a new order using the provided list of products.
    //and also notify picker and orderTracker
    public Order newOrder(ArrayList<Product> trolley) throws IOException, SQLException {
        int orderId = OrderCounter.generateOrderId(); //get unique orderId
        String orderedDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        //make an Order Object: id, Ordered_state, orderedDateTime, and productsList(trolley)
        Order theOrder = new Order(orderId,OrderState.Ordered,orderedDateTime,trolley);

        //write order details to file for the orderId in orderedPath (ie. orders/ordered)
        String orderDetail = theOrder.orderDetails();
        Path path = orderedPath;
        OrderFileManager.createOrderFile(path, orderId, orderDetail);

        orderMap.put(orderId, theOrder.getState()); //add the order to orderMap,state is Ordered initially
        notifyOrderTrackers(); //notify OrderTrackers
        notifyPickerModels();//notify pickers
        
        return theOrder;
    }

    //Registers an OrderTracker to receive updates about changes.
    public void registerOrderTracker(OrderTracker orderTracker){
        orderTrackerList.add(orderTracker);
    }
     //Notifies all registered observer_OrderTrackers to update and display the latest orderMap.
    public void notifyOrderTrackers(){
        for(OrderTracker orderTracker : orderTrackerList){
            orderTracker.setOrderMap(orderMap);
        }
    }

    //Registers a PickerModel to receive updates about changes.
    public void registerPickerModel(PickerModel pickerModel){
        pickerModelList.add(pickerModel);
    }

    //notify all pickers to show orderMap (only ordered and progressing states orders)
    public void notifyPickerModels(){
        TreeMap<Integer,OrderState> orderMapForPicker = new TreeMap<>();
        progressingOrderMap = filterOrdersByState(OrderState.Progressing);
        OrderedOrderMap = filterOrdersByState(OrderState.Ordered);
        orderMapForPicker.putAll(progressingOrderMap);
        orderMapForPicker.putAll(OrderedOrderMap);
        for(PickerModel pickerModel : pickerModelList){
            pickerModel.setOrderMap(orderMapForPicker);
        }
    }

    // Filters orderMap that match the specified state, a helper class used by notifyPickerModel()
    private TreeMap<Integer, OrderState> filterOrdersByState(OrderState state) {
        TreeMap<Integer, OrderState> filteredOrderMap = new TreeMap<>(); // New map to hold filtered orders
        // Loop through the orderMap and add matching orders to filteredOrders
        for (Map.Entry<Integer, OrderState> entry : orderMap.entrySet()) {
            if (entry.getValue() == state) {
                filteredOrderMap.put(entry.getKey(), entry.getValue());
            }
        }
        return filteredOrderMap;
    }

    //Changes the state of the specified order, updates its file, and moves it to the appropriate folder.
    //trigger by PickerModel
    public void changeOrderStateMoveFile(int orderId, OrderState newState) throws IOException {
        if(orderMap.containsKey(orderId) && !orderMap.get(orderId).equals(newState))
        {
            switch(newState){
                case OrderState.Progressing:
                    OrderFileManager.updateAndMoveOrderFile(orderId, newState,orderedPath,progressingPath);
                    break;
                case OrderState.Collected:
                    OrderFileManager.updateAndMoveOrderFile(orderId, newState,progressingPath,collectedPath);
                    removeCollectedOrder(orderId);
                    break;
            }

            orderMap.put(orderId, newState);
            notifyOrderTrackers();
            notifyPickerModels();
        }
    }


    /**
     * Removes collected orders from the system after they have been collected for 10 seconds.
     *
     * This ensures that collected orders are cleared from the active order pool and are no longer displayed
     * by the OrderTracker after the brief period. This keeps the system focused on orders in the
     * "ordered" and "progressing" states.
     * The 10-second delay gives enough time for any final updates, and providing a short window for review of completed orders.
     */
    private void removeCollectedOrder(int orderId) {
        if (orderMap.containsKey(orderId)) {
            // Schedule removal after a few seconds
            scheduler.schedule(() -> {
                orderMap.remove(orderId); //remove collected order
                System.out.println("Order " + orderId + " removed from tracker and OrdersMap.");
                notifyOrderTrackers();
            }, 10, TimeUnit.SECONDS );
        }
    }

    // Reads details of an order for display in the picker once they started preparing the order.
    public String  getOrderDetailForPicker(int orderId) throws IOException {
        OrderState state = orderMap.get(orderId);
        if(state.equals(OrderState.Progressing)) {
            return OrderFileManager.readOrderFile(progressingPath,orderId);
        }else{
            return "the fuction is only for picker";
        }
    }

    //Initializes the internal order map by loading the uncollected orders from the file system.
    // Called during system startup by the Main class.
    public void initializeOrderMap(){
        ArrayList<Integer> orderedIds = orderIdsLoader(orderedPath);
        ArrayList<Integer> progressingIds = orderIdsLoader(progressingPath);
        if(orderedIds.size()>0){
            for(Integer orderId : orderedIds){
                orderMap.put(orderId, OrderState.Ordered);
            }
        }
        if(progressingIds.size()>0){
            for(Integer orderId : progressingIds){
                orderMap.put(orderId, OrderState.Progressing);
            }
        }
        notifyOrderTrackers();
        notifyPickerModels();
        System.out.println("orderMap initilized. "+ orderMap.size() + " orders in total, including:");
        System.out.println( orderedIds.size() + " Ordered orders, " +progressingIds.size() + " Progressing orders " );
    }

    // Loads a list of order IDs from the specified directory.
    // Used internally by initializeOrderMap().
    private ArrayList<Integer> orderIdsLoader(Path dir) {
        ArrayList<Integer> orderIds = new ArrayList<>();

        if (Files.exists(dir) && Files.isDirectory(dir)) {
            try (Stream<Path> fileStream = Files.list(dir)) {
                // Process the stream without checking it separately
                List<Path> files = fileStream.filter(Files::isRegularFile).toList();

                if (files.isEmpty()) {
                    System.out.println(dir + " is empty");
                } else {
                    for (Path file : files) {
                        String fileName = file.getFileName().toString();
                        if (fileName.endsWith(".txt")) { // Ensure it's a .txt file
                            try {
                                int orderId = Integer.parseInt(fileName.substring(0, fileName.lastIndexOf('.')));
                                orderIds.add(orderId);
                                System.out.println(orderId);
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid file name: " + fileName);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error reading " + dir + ", " + e.getMessage());
            }
        } else {
            System.out.println(dir + " does not exist.");
        }
        return orderIds;
    }

}
