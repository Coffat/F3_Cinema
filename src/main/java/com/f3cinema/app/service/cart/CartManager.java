package com.f3cinema.app.service.cart;

import com.f3cinema.app.dto.ProductDTO;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the Shopping Cart state (Observer Subject).
 */
public class CartManager {

    private static final CartManager INSTANCE = new CartManager();
    private final Map<ProductDTO, Integer> items = new LinkedHashMap<>();
    private final List<CartObserver> observers = new ArrayList<>();

    private CartManager() {}

    public static CartManager getInstance() {
        return INSTANCE;
    }

    public void addObserver(CartObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(CartObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        for (CartObserver o : observers) {
            o.onCartUpdated();
        }
    }

    public Map<ProductDTO, Integer> getItems() {
        return items;
    }

    public int getQuantity(ProductDTO product) {
        return items.getOrDefault(product, 0);
    }

    public void addItem(ProductDTO product, int quantity) {
        if (quantity <= 0) return;
        items.put(product, getQuantity(product) + quantity);
        notifyObservers();
    }

    public void updateQuantity(ProductDTO product, int quantity) {
        if (quantity <= 0) {
            removeItem(product);
        } else {
            items.put(product, quantity);
            notifyObservers();
        }
    }

    public void removeItem(ProductDTO product) {
        if (items.containsKey(product)) {
            items.remove(product);
            notifyObservers();
        }
    }

    public void clearCart() {
        items.clear();
        notifyObservers();
    }

    public BigDecimal getTotalPrice() {
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<ProductDTO, Integer> entry : items.entrySet()) {
            total = total.add(entry.getKey().price().multiply(BigDecimal.valueOf(entry.getValue())));
        }
        return total;
    }
}
