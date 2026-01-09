package ci553.happyshop.utility;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class centralizes the location of all external storage folders and files used by the system.
 * It provides both String and Path representations for accessing and manipulating files and directories.
 *
 * Key responsibilities:
 *
 * 1. Image Storage:
 *    - imageFolder / imageFolderPath:
 *         The working folder for storing product images used in the live system.
 *    - imageResetFolder / imageResetFolderPath:
 *         A backup folder containing original product images.
 *         Used to restore image content when resetting the database.
 *
 * 2. Order Management:
 *    - ordersFolder / ordersPath:
 *         Root folder for all orders.
 *    - orderedPath:
 *         Subfolder to store orders in the "Ordered" state.
 *    - progressingPath:
 *         Subfolder to store orders in the "Progressing" state (e.g., being prepared by a picker).
 *    - collectedPath:
 *         Subfolder to store orders in the "Collected" state (e.g., customer collected).
 *
 * 3. Order ID Tracking:
 *    - orderCounterFile / orderCounterPath:
 *         A text file (orders/orderCounter.txt) used to track and increment the unique order ID
 *         when a new order is created.
 *
 * These static paths ensure consistent folder usage throughout the application and simplify
 * file-related operations such as reset, loading, and persistence.
 *
 * By centralizing all locations for external storage in this class,
 * any changes to these directory only need to be made here.
 * This avoids the need to modify the path throughout the entire codebase.
 */

public class StorageLocation {
    /**
     * Note: The trailing '/' in "images/" is important.
     * It does not affect Path conversion, but is necessary for correct String operations,
     * such as: String relativeImageUrl = StorageLocation.imageFolder + imageName;
     */

    // working Image folder
    public static final String imageFolder = "images/";
    public static final Path imageFolderPath = Paths.get(imageFolder);

    // Backup image folder — used to restore images when resetting the database
    public static final String imageResetFolder = "images_resetDB";
    public static final Path imageResetFolderPath = Paths.get(imageResetFolder);

    // Orders folders and their Path
    public static final String ordersFolder = java.nio.file.Paths.get("orders").toAbsolutePath().toString() + java.io.File.separator;
    public static final Path ordersPath = Paths.get(ordersFolder);
    public static final Path orderedPath = ordersPath.resolve("ordered");//orders/ordered to store orders at Ordered state
    public static final Path progressingPath = ordersPath.resolve("progressing");// orders/progressing to store orders at Progressing state
    public static final Path collectedPath = ordersPath.resolve("collected");//orders/collected to store orders at Collected state

    //OrderCounter File and its Path, ie orders/orderCounter.txt
    public static final String orderCounterFile = "orderCounter.txt";
    public static final Path orderCounterPath = ordersPath.resolve(orderCounterFile);
}
