package com.nanoCurcuminWeb.repository;

import com.nanoCurcuminWeb.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Cart findByUserId(Long userId);
}
