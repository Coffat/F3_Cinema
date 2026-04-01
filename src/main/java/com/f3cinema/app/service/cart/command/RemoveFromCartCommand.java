package com.f3cinema.app.service.cart.command;

import com.f3cinema.app.dto.ProductDTO;
import com.f3cinema.app.service.cart.CartManager;

public class RemoveFromCartCommand implements CartCommand {
    private final ProductDTO product;

    public RemoveFromCartCommand(ProductDTO product) {
        this.product = product;
    }

    @Override
    public void execute() {
        CartManager.getInstance().removeItem(product);
    }
}
