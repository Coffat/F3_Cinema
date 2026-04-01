package com.f3cinema.app.service.cart.command;

import com.f3cinema.app.service.cart.CartManager;

public class ClearCartCommand implements CartCommand {

    @Override
    public void execute() {
        CartManager.getInstance().clearCart();
    }
}
