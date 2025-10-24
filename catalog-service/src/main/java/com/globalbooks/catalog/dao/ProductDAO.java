package com.globalbooks.catalog.dao;

import com.globalbooks.catalog.model.*;
import java.util.List;

public interface ProductDAO {
    Product findById(String productId);
    List<Product> findAll();
    List<Product> search(SearchCriteria criteria);
    InventoryStatus getInventoryStatus(String productId);
    boolean updateInventory(String productId, int quantity, String operation);
    boolean save(Product product);
    boolean update(Product product);
    boolean delete(String productId);
}