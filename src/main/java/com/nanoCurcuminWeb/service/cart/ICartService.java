package com.nanoCurcuminWeb.service.cart;

import com.nanoCurcuminWeb.dto.CartDto;
import com.nanoCurcuminWeb.model.Cart;
import com.nanoCurcuminWeb.model.User;

import java.math.BigDecimal;

public interface ICartService {
    Cart getCart(Long id);
    void clearCart(Long id);
    BigDecimal getTotalPrice(Long id);

    Cart initializeNewCart(User user);

    Cart getCartByUserId(Long userId);

    CartDto convertToDto(Cart cart);
}
