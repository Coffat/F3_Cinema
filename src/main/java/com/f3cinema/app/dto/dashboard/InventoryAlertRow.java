package com.f3cinema.app.dto.dashboard;

/**
 * Product below minimum stock threshold.
 */
public record InventoryAlertRow(String productName, int currentQuantity, int minThreshold) {
}
