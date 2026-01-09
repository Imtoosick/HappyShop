package ci553.happyshop.storageAccess;

import ci553.happyshop.catalogue.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/** ProductTable definition
 * "CREATE TABLE ProductTable(" +
 *         "productID CHAR(4) PRIMARY KEY," +
 *         "description VARCHAR(100)," +
 *         "unitPrice DOUBLE," +
 *         "image VARCHAR(100)," +
 *         "inStock INT," +
 *         "CHECK (inStock >= 0)" +
 *           ")",
 */

public class DerbyRW implements DatabaseRW {
    private static String dbURL = DatabaseRWFactory.dbURL; // Shared by all instances
    private  Lock lock = new ReentrantLock(); // Each instance has its own lock

    //search product by product Id or name, return a list of products or null
    //search by Id at first, if get null, search by product name
    //currently used by warehouseModel.
    // try to use this method to upgrade customer client so that user can search by id and name
    public ArrayList<Product> getAllProductsSortedByName() throws SQLException {
        ArrayList<Product> productList = new ArrayList<>();
        String query = "SELECT * FROM ProductTable ORDER BY LOWER(description) ASC";

        try (Connection conn = DriverManager.getConnection(dbURL);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                productList.add(makeProObjFromDbRecord(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }

        return productList;
    }

    public ArrayList<Product> searchProduct(String keyword) throws SQLException {
        ArrayList<Product> productList = new ArrayList<>();

        // searching by product ID at first
        Product product = searchByProductId(keyword);
        if (product != null) {
            productList.add(product);
        } else { // If no products found by ID, searching by product name
            productList = searchByProName(keyword);
        }

        // If still no products found, print a message
        if (productList.isEmpty()) {
            System.out.println("Product " + keyword + " not found.");
        }
        return productList;
    }

    //search  by product Id, return a product or null
    public Product searchByProductId(String proId) throws SQLException {
        Product product = null;
        String query = "SELECT * FROM ProductTable WHERE productID = ?";

        try (Connection conn = DriverManager.getConnection(dbURL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            // Set the productId parameter
            pstmt.setString(1, proId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()){
                    product= makeProObjFromDbRecord(rs);
                    System.out.println("Product " + proId + " found.");
                }else{
                    System.out.println("Product " + proId + " not found.");
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return product;
    }

    //helper method
    //search  by product name, return a List of products or null
    private ArrayList<Product> searchByProName(String name) {
        ArrayList<Product> productList = new ArrayList<>();
        String query = "SELECT * FROM ProductTable WHERE LOWER(description) LIKE LOWER(?)";

        try (Connection conn = DriverManager.getConnection(dbURL);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + name.toLowerCase() + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    productList.add(makeProObjFromDbRecord(rs)); // Add all matching products to list
                }

                if (productList.isEmpty()) {
                    System.out.println("Product " + name + " not found.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Database query error, search by name: " + name + " " + e.getMessage());
        }

        return productList; // could be empty if no matches
    }

    //make a Product object from the database record
    private Product makeProObjFromDbRecord(ResultSet rs) throws SQLException {
        Product product = null;
        String productId = rs.getString("productID");
        String description = rs.getString("description");
        String imagePath = rs.getString("image");
        double unitPrice = rs.getDouble("unitPrice");
        int inStock = rs.getInt("inStock");
        product =new Product(productId,description,imagePath,unitPrice,inStock);

        // Show product details
        System.out.println("Product ID: " + productId);
        System.out.println("Description: " + description);
        System.out.println("Image: " + imagePath);
        System.out.println("unitPrice: " + unitPrice);

        // Check availability and display message
        if(inStock <= 0){
            System.out.println("Product " + productId+ " is NOT in stock");
        }
        else if(inStock < 10) {
            System.out.println("Product " + productId+ "low stock warning!" + inStock + " units left.");
        }
        else {
            System.out.println("Product " + productId+ " is available");
        }

        System.out.println("-----"); // Divider for readability
        return product;
    }

    public ArrayList<Product> purchaseStocks(ArrayList<Product> proList) throws SQLException {
        lock.lock();  // Lock the critical section to prevent concurrent access
        ArrayList<Product> insufficientProducts = new ArrayList<>();

        String checkSql = "SELECT inStock FROM ProductTable WHERE productId = ?";
        String updateSql = "UPDATE ProductTable SET inStock = inStock - ? WHERE productId = ?";

        // Use try-with-resources for Connection and PreparedStatements
        try (Connection conn = DriverManager.getConnection(dbURL)) {
            conn.setAutoCommit(false); // Turn off auto-commit for transaction

            // Use a second try-with-resources for the PreparedStatements
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                 PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

                boolean allSufficient = true; // Flag to track if all products have sufficient stock

                for (Product product : proList) {
                    checkStmt.setString(1, product.getProductId());
                    ResultSet rs = checkStmt.executeQuery();

                    if (rs.next()) {
                        int currentStock = rs.getInt("inStock");
                        int newStock = currentStock - product.getOrderedQuantity();

                        // Debugging: Print values before update
                        System.out.println("Product ID: " + product.getProductId());
                        System.out.println("Before change: " + currentStock);
                        System.out.println("Quantity Ordered: " + product.getOrderedQuantity());

                        if (newStock >= 0) { // Ensure stock doesn't go negative
                            updateStmt.setInt(1, product.getOrderedQuantity());
                            updateStmt.setString(2, product.getProductId());
                            updateStmt.addBatch();

                            // Debugging: Print values after update
                            System.out.println("After change: " + newStock);
                            System.out.println("Update successful for Product ID: " + product.getProductId());
                        } else {
                            insufficientProducts.add(product);
                            allSufficient = false; // Mark that there's at least one insufficient product
                            System.out.println("Not enough stock for Product ID: " + product.getProductId());
                        }
                        System.out.println("--------------------------------");
                    }
                }

                if (allSufficient) {
                    // If all products have sufficient stock, execute the batch and commit
                    updateStmt.executeBatch();
                    conn.commit();  // Commit all updates if all updates succeed
                    System.out.println("Database update successful.");
                } else {
                    // If there's insufficient stock for any product, rollback the entire transaction
                    conn.rollback();
                    System.out.println("Insufficient stock for some products, all updates rolled back.");
                }

            } catch (SQLException e) {
                conn.rollback();  // Rollback if anything failed inside
                System.out.println("Database update error, update failed");
            }
        } finally {
            lock.unlock(); // Always release the lock after the operation
        }

        return insufficientProducts;
    }


    //warehouse edits an existing product
    public void updateProduct(String id, String des, double price, String iName, int stock) throws SQLException {
        lock.lock();
        String selectSql = "SELECT * FROM ProductTable WHERE productID = ?";
        String updateSql = "UPDATE ProductTable SET " +
                "description = ?, " +
                "unitPrice = ?, " +
                "image = ?, "+
                "inStock = ? " +
                "WHERE productID = ?";

        try (Connection conn = DriverManager.getConnection(dbURL);
             PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            // Print Before Update
            selectStmt.setString(1, id);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Before Update:");
                    System.out.println("ID: " + rs.getString("productID"));
                    System.out.println("Description: " + rs.getString("description"));
                    System.out.println("Unit Price: " + rs.getDouble("unitPrice"));
                    System.out.println("Stock: " + rs.getInt("inStock"));
                    System.out.println("Image: " + rs.getString("image"));
                } else {
                    System.out.println("Product not found: " + id);
                    return; // Exit if product doesn't exist
                }
            }

            // Perform Update
            updateStmt.setString(1, des);
            updateStmt.setDouble(2, price);
            updateStmt.setString(3, iName);
            updateStmt.setInt(4, stock);
            updateStmt.setString(5, id);
            updateStmt.executeUpdate();

            // Print After Update
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("After Update:");
                    System.out.println("ID: " + rs.getString("productID"));
                    System.out.println("Description: " + rs.getString("description"));
                    System.out.println("Unit Price: " + rs.getDouble("unitPrice"));
                    System.out.println("Stock: " + rs.getInt("inStock"));
                    System.out.println("image: " + rs.getString("image"));
                }
            }
        }
        finally {
            lock.unlock(); // Always release the lock after the operation
        }
    }

//warehouse delete an existing product
    public void deleteProduct(String proId) throws SQLException {
        lock.lock();
        String selectSql = "SELECT * FROM ProductTable WHERE productID = ?";
        String deleteSql = "DELETE FROM ProductTable WHERE productID = ?";

        try (Connection conn = DriverManager.getConnection(dbURL);
             PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
            conn.setAutoCommit(true); // Set auto-commit to true immediately

            // print product details before deletion
            selectStmt.setString(1, proId);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Before delete:");
                    System.out.println("ID: " + rs.getString("productID"));
                    System.out.println("Description: " + rs.getString("description"));
                    System.out.println("Unit Price: " + rs.getDouble("unitPrice"));
                    System.out.println("Stock: " + rs.getInt("inStock"));
                } else {
                    System.out.println("Product not found: " + proId);
                    return; // Exit if product does not exist
                }
            }

            // delete from database
            deleteStmt.setString(1, proId);
            deleteStmt.executeUpdate();
            System.out.println("Product " + proId + " deleted from database.");
        }

        finally {
            lock.unlock(); // Always release the lock after the operation
        }
    }

    //check if product ID is unique
    //warehouse tries to add a new prodcut, id must be unique
    public boolean isProIdAvailable(String proId) throws SQLException {
        String query = "SELECT COUNT(*) FROM ProductTable WHERE productID = ?";
                             //the count of records that match the given proId.
        try (Connection conn = DriverManager.getConnection(dbURL);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, proId);
            ResultSet rs = stmt.executeQuery();
            // the rs is the COUNT(*) result (a single number): how many records that match the given proId.
            // If count > 0, the ID is already in the database, so it's not available, return false
            // If count = 0, the ID is available, return true
            if (rs.next()) { // Move cursor to the first (and only) row
                int count = rs.getInt(1); // Get the first column value (the count)
                if (count == 0) return true;
                else return false;
            }
            return false; // Default case (should not happen)
        }
    }

    //   /images/0001TV.jpg
    //warehouse adds a new product to database
    public void insertNewProduct(String id, String des,double price,String image,int stock) throws SQLException {
        lock.lock();
        String insertSql = "INSERT INTO ProductTable VALUES(?, ?, ?, ?, ?)";
        String selectSql = "SELECT * FROM ProductTable WHERE productID = ?";
        try (Connection conn = DriverManager.getConnection(dbURL);
        PreparedStatement insertStmt = conn.prepareStatement(insertSql);
        PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            conn.setAutoCommit(true); // Set auto-commit to true immediately
            insertStmt.setString(1, id);
            insertStmt.setString(2, des);
            insertStmt.setDouble(3, price);
            insertStmt.setString(4, image);
            insertStmt.setInt(5, stock);
            selectStmt.setString(1, id);
            insertStmt.executeUpdate();
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) { //print the inserted record
                System.out.println("Insert successful for Product ID: \" + id");
                System.out.println("ID: " + rs.getString("productID"));
                System.out.println("Description: " + rs.getString("description"));
                System.out.println("Unit Price: " + rs.getDouble("unitPrice"));
                System.out.println("Stock: " + rs.getInt("inStock"));
            }
        }
        finally {
            lock.unlock(); // Always release the lock after the operation
        }
    }

}
