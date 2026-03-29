package com.f3cinema.app.service;

import com.f3cinema.app.dto.ProductDTO;
import java.util.List;

public interface InventoryService {
    List<ProductDTO> getAllInventory();
    void addProduct(ProductDTO dto);
    void addStock(Long productId, int quantity);
}
