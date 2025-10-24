package com.globalbooks.catalog.dao;

import com.globalbooks.catalog.model.*;
import com.globalbooks.catalog.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAOImpl implements ProductDAO {

    private static final Logger logger = LoggerFactory.getLogger(ProductDAOImpl.class);

    @Override
    public Product findById(String productId) {
        String sql = "SELECT * FROM products WHERE product_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
        } catch (SQLException e) {
            logger.error("Error finding product by ID: {}", productId, e);
        }
        return null;
    }

    @Override
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY title";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all products", e);
        }
        return products;
    }

    @Override
    public List<Product> search(SearchCriteria criteria) {
        List<Product> products = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // Build dynamic query based on criteria
        if (criteria.getKeyword() != null && !criteria.getKeyword().isEmpty()) {
            sql.append(" AND (LOWER(title) LIKE ? OR LOWER(description) LIKE ?)");
            String keyword = "%" + criteria.getKeyword().toLowerCase() + "%";
            params.add(keyword);
            params.add(keyword);
        }

        if (criteria.getCategory() != null && !criteria.getCategory().isEmpty()) {
            sql.append(" AND LOWER(category) = ?");
            params.add(criteria.getCategory().toLowerCase());
        }

        if (criteria.getAuthor() != null && !criteria.getAuthor().isEmpty()) {
            sql.append(" AND LOWER(author) LIKE ?");
            params.add("%" + criteria.getAuthor().toLowerCase() + "%");
        }

        if (criteria.getMinPrice() != null) {
            sql.append(" AND price >= ?");
            params.add(criteria.getMinPrice());
        }

        if (criteria.getMaxPrice() != null) {
            sql.append(" AND price <= ?");
            params.add(criteria.getMaxPrice());
        }

        if (criteria.isInStockOnly()) {
            sql.append(" AND stock_quantity > 0");
        }

        sql.append(" ORDER BY title LIMIT ?");
        params.add(criteria.getMaxResults());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    stmt.setString(i + 1, (String) param);
                } else if (param instanceof BigDecimal) {
                    stmt.setBigDecimal(i + 1, (BigDecimal) param);
                } else if (param instanceof Integer) {
                    stmt.setInt(i + 1, (Integer) param);
                }
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            logger.error("Error searching products", e);
        }
        return products;
    }

    @Override
    public InventoryStatus getInventoryStatus(String productId) {
        String sql = "SELECT product_id, stock_quantity, reserved_quantity, " +
                "warehouse_location, restock_date FROM products WHERE product_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                InventoryStatus status = new InventoryStatus();
                status.setProductId(rs.getString("product_id"));
                int stockQty = rs.getInt("stock_quantity");
                int reservedQty = rs.getInt("reserved_quantity");
                status.setAvailableQuantity(stockQty - reservedQty);
                status.setReservedQuantity(reservedQty);
                status.setInStock(stockQty > reservedQty);
                status.setWarehouseLocation(rs.getString("warehouse_location"));

                Date restockDate = rs.getDate("restock_date");
                if (restockDate != null) {
                    status.setRestockDate(new java.util.Date(restockDate.getTime()));
                }
                status.setLastUpdated(new java.util.Date());

                return status;
            }
        } catch (SQLException e) {
            logger.error("Error getting inventory status for product: {}", productId, e);
        }
        return null;
    }

    @Override
    public boolean updateInventory(String productId, int quantity, String operation) {
        String sql = "";

        switch (operation) {
            case "RESERVE":
                sql = "UPDATE products SET reserved_quantity = reserved_quantity + ? " +
                        "WHERE product_id = ? AND stock_quantity >= reserved_quantity + ?";
                break;
            case "RELEASE":
                sql = "UPDATE products SET reserved_quantity = GREATEST(0, reserved_quantity - ?) " +
                        "WHERE product_id = ?";
                break;
            case "DEDUCT":
                sql = "UPDATE products SET stock_quantity = stock_quantity - ?, " +
                        "reserved_quantity = GREATEST(0, reserved_quantity - ?) " +
                        "WHERE product_id = ? AND stock_quantity >= ?";
                break;
            default:
                logger.error("Invalid operation: {}", operation);
                return false;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (operation.equals("RESERVE")) {
                stmt.setInt(1, quantity);
                stmt.setString(2, productId);
                stmt.setInt(3, quantity);
            } else if (operation.equals("RELEASE")) {
                stmt.setInt(1, quantity);
                stmt.setString(2, productId);
            } else if (operation.equals("DEDUCT")) {
                stmt.setInt(1, quantity);
                stmt.setInt(2, quantity);
                stmt.setString(3, productId);
                stmt.setInt(4, quantity);
            }

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Error updating inventory for product: {}", productId, e);
            return false;
        }
    }

    @Override
    public boolean save(Product product) {
        String sql = "INSERT INTO products (product_id, title, author, isbn, description, " +
                "category, price, currency, stock_quantity, publish_date, image_url) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setProductParameters(stmt, product);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Error saving product: {}", product.getProductId(), e);
            return false;
        }
    }

    @Override
    public boolean update(Product product) {
        String sql = "UPDATE products SET title = ?, author = ?, isbn = ?, description = ?, " +
                "category = ?, price = ?, currency = ?, stock_quantity = ?, " +
                "publish_date = ?, image_url = ? WHERE product_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, product.getTitle());
            stmt.setString(2, product.getAuthor());
            stmt.setString(3, product.getIsbn());
            stmt.setString(4, product.getDescription());
            stmt.setString(5, product.getCategory());
            stmt.setBigDecimal(6, product.getPrice());
            stmt.setString(7, product.getCurrency());
            stmt.setInt(8, product.getStockQuantity());

            if (product.getPublishDate() != null) {
                stmt.setDate(9, new Date(product.getPublishDate().getTime()));
            } else {
                stmt.setNull(9, Types.DATE);
            }

            stmt.setString(10, product.getImageUrl());
            stmt.setString(11, product.getProductId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Error updating product: {}", product.getProductId(), e);
            return false;
        }
    }

    @Override
    public boolean delete(String productId) {
        String sql = "DELETE FROM products WHERE product_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, productId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Error deleting product: {}", productId, e);
            return false;
        }
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getString("product_id"));
        product.setTitle(rs.getString("title"));
        product.setAuthor(rs.getString("author"));
        product.setIsbn(rs.getString("isbn"));
        product.setDescription(rs.getString("description"));
        product.setCategory(rs.getString("category"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setCurrency(rs.getString("currency"));
        product.setStockQuantity(rs.getInt("stock_quantity"));

        Date publishDate = rs.getDate("publish_date");
        if (publishDate != null) {
            product.setPublishDate(new java.util.Date(publishDate.getTime()));
        }

        product.setImageUrl(rs.getString("image_url"));
        return product;
    }

    private void setProductParameters(PreparedStatement stmt, Product product) throws SQLException {
        stmt.setString(1, product.getProductId());
        stmt.setString(2, product.getTitle());
        stmt.setString(3, product.getAuthor());
        stmt.setString(4, product.getIsbn());
        stmt.setString(5, product.getDescription());
        stmt.setString(6, product.getCategory());
        stmt.setBigDecimal(7, product.getPrice());
        stmt.setString(8, product.getCurrency());
        stmt.setInt(9, product.getStockQuantity());

        if (product.getPublishDate() != null) {
            stmt.setDate(10, new Date(product.getPublishDate().getTime()));
        } else {
            stmt.setNull(10, Types.DATE);
        }

        stmt.setString(11, product.getImageUrl());
    }
}