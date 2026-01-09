package ci553.happyshop.storageAccess;

import ci553.happyshop.orderManagement.OrderState;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * This class manages creation, updating, and relocation of order files.
 * <p>
 * This class is used by the {@code OrderHub} class to manage file-based representation of orders.
 * Each order is stored as a text file in a state-specific folder (e.g., ordered, progressing, or collected).
 * </p>
 *
 * <p>
 * When a new order is created, a file (e.g.,12.txt) is generated and placed in the "ordered" folder.
 * The content of the file follows this structure:
 *  <pre>
 *  OrderId: 12
 *  State: Ordered
 *  OrderedDateTime: 2025-03-11 19:53:45
 *  ProgressingDateTime:
 *  CollectedDateTime:
 *  Items:
 *  0001 apples  x2 (£3.00)
 *  0002 TV      x1 (£999.99)
 *  Total price: £1002.99
 *  </pre>
 * </p>
 *
 * <p>
 * When the order state changes, this class updates the corresponding timestamp and moves the file to the appropriate folder.
 *  1. update state from Ordered to Progressing, (then move the file to progressing folder)
 *  2. update state from Progressing to Collected (then move the file to collected folder)
 * </p>
 */

public class OrderFileManager {

    //Creates a new order file in the specified directory with the given content.
    public static void createOrderFile(Path dir, int orderId, String orderDetail) throws IOException {
        String orderFileName = String.valueOf(orderId)+".txt";
        Path path = dir.resolve(orderFileName); // eg. orders/ordered/12.txt
        if(Files.notExists(path)) {
            Files.createFile(path);
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                writer.write(orderDetail);
                writer.newLine();
                System.out.println(path + " created");
            }
        }
        else{
            System.out.println(path + " already exists");
        }
    }

    // Updates the order's state and corresponding timestamp, then moves the order file to the new state folder.
    //Ordered state in orders/ordered
    //Progressing state in orders/progressing
    //Collected state in orders/collected
    public static boolean updateAndMoveOrderFile(int orderId, OrderState newState, Path sourceDir, Path targetDir) throws IOException {
        String orderFileName = String.valueOf(orderId)+".txt";
        Path sourcePath = sourceDir.resolve(orderFileName);
        Path targetPath = targetDir.resolve(orderFileName);

        Files.createDirectories(sourceDir);
        Files.createDirectories(targetDir);

        if (Files.exists(sourcePath)) {
            updateOrderStateAndTime(sourceDir,orderId,newState);
            if(!sourceDir.equals(targetDir))
                Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return true;
        }
        else{
            System.out.println(sourcePath + " not found in ");
            return false;
        }
    }


    /**
     * Updates the state and timestamp field inside the order file.
     * This method creates a temporary file with the updated content and replaces
     * the original file once updates are complete.
     */
    private static void updateOrderStateAndTime(Path sourceDir, int orderId, OrderState newState) throws IOException {
        String orderFileName = String.valueOf(orderId)+".txt";
        String tempFileName = String.valueOf(orderId) + "_temp.txt";
        Path sourcePath = sourceDir.resolve(orderFileName);
        Path tempFilePath = sourceDir.resolve(tempFileName);

        try (BufferedReader reader = Files.newBufferedReader(sourcePath, StandardCharsets.UTF_8);
             BufferedWriter writer = Files.newBufferedWriter(tempFilePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("State")) {
                    line = "State: " + newState;
                } else if (newState.equals(OrderState.Progressing) && line.startsWith("ProgressingDateTime")) {
                    line = "ProgressingDateTime: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } else if (newState.equals(OrderState.Collected) && line.startsWith("CollectedDateTime")) {
                    line = "CollectedDateTime: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
                writer.write(line);
                writer.newLine();
            }
        }

        // Replace the original file with the updated temp file
        try {
            Files.move(tempFilePath, sourcePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Move failed: " + e.getMessage());
            Files.deleteIfExists(tempFilePath); // Delete temp file ONLY IF move failed and it still exists
        }
    }

    //Reads the content of an order file as a single string.
    public static String readOrderFile(Path dir, int orderId) throws IOException {
        String orderFileName = String.valueOf(orderId)+".txt";
        Path path = dir.resolve(orderFileName);
        // Check if the file exists before reading
        if (!Files.exists(path)) {
            throw new IOException("Order file not found: " + path);
        }
        return String.join("\n", Files.readAllLines(path));
    }
}
