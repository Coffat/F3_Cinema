package com.f3cinema.app.service.cart.command;

import com.f3cinema.app.dto.ProductDTO;
import com.f3cinema.app.service.cart.CartManager;

public class UpdateQuantityCommand implements CartCommand {
    private final ProductDTO product;
    private final int quantity;

    public UpdateQuantityCommand(ProductDTO product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    @Override
    public void execute() {
        CartManager.getInstance().updateQuantity(product, quantity);
    }
}
