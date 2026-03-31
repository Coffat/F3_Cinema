package com.f3cinema.app.service;

import com.f3cinema.app.dto.ProductDTO;
import com.f3cinema.app.dto.dashboard.InventoryAlertRow;
import com.f3cinema.app.service.impl.InventoryServiceImpl;
import java.util.List;

/**
 * InventoryService — Business logic for inventory management.
 * Unified interface merging local work (Product Management) 
 * and Huy's work (Dashboard Alerts).
 */
public interface InventoryService {
    
    static InventoryService getInstance() {
        return InventoryServiceImpl.getInstance();
    }
    
    // Local additions (Product Management)
    List<ProductDTO> getAllInventory();
    void addProduct(ProductDTO dto);
    void addStock(Long productId, int quantity);
    
    // Huy's additions (Dashboard Alerts)
    List<InventoryAlertRow> getLowStockAlerts();
}
