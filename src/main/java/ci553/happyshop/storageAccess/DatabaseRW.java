package ci553.happyshop.storageAccess;

import ci553.happyshop.catalogue.Product;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * The DatabaseRW interface defines the contract for interacting with the product database.
 * It is currently implemented by the DerbyRW class, which provides the actual functionality.
 *
 * Responsibilities:
 * - Searching for products by keyword or product ID.
 * - Performing stock updates and validations during purchases.
 * - Updating, deleting, or inserting products.
 * - Checking whether a product ID is available before insertion.
 *
 * Why use this interface:
 * Introducing this interface allows for better separation of specification and implementation,
 * making the system more adaptable and maintainable.
 *
 * 1. **Ease of Substitution**: If the application switches to a different database system
 *    (e.g., from Derby to MySQL or SQLite), a new implementation can be provided without affecting other components.
 *
 * 2. **Improved Testability**: During unit testing, mock implementations can simulate database behavior,
 *    enabling effective testing without requiring a live database connection.
 */

public interface DatabaseRW {

    /**
     * Searches for products by a keyword, which may match the product ID or appear in the description.
     *
     * @param keyword the keyword to search for
     * @return a list of products matching the keyword
     * @throws SQLException if a database access error occurs
     */
    ArrayList<Product> searchProduct(String keyword) throws SQLException;

    /**
     * Searches for a product by its unique product ID.
     * @param productId the product ID
     * @return the matching product, or null if not found
     */
    Product searchByProductId(String productId) throws SQLException;
    ArrayList<Product> getAllProductsSortedByName() throws SQLException;
    /**
     * Attempts to purchase (reduce stocks of) the given list of products.
     * Behavior:
     * - If all requested quantities are available, stocks are reduced and an empty list is returned.
     * - If any product does not have sufficient stock, no stock is updated and a list of all insufficient products is returned.
     *
     * @param proList the list of products with requested quantities to purchase
     */
    ArrayList<Product> purchaseStocks(ArrayList<Product> proList) throws SQLException;


    /**
     * Updates the details of a product identified by its ID.
     *
     * @param id         the product ID
     * @param des        the new description
     * @param price      the new price
     * @param imageName  the new image file name
     * @param stock      the updated stock quantity
     */
    void updateProduct(String id, String des, double price, String imageName, int stock) throws SQLException;


    // Deletes a product identified by its ID.
    void deleteProduct(String id) throws SQLException;

    /**
     * Inserts a new product into the database.
     * @param id      the product ID
     * @param des     the product description
     * @param price   the product price
     * @param image   the image file name
     * @param stock   the initial stock quantity
     */
    void insertNewProduct(String id, String des, double price, String image, int stock) throws SQLException;

    /**
     * Checks whether the given product ID is available for use (i.e., not already in use).
     * @param productId the product ID to check
     * @return true if the ID is available, false if it already exists in the database
     */
    boolean isProIdAvailable(String productId) throws SQLException;
}


