package com.nanoCurcuminWeb.controller;

import com.nanoCurcuminWeb.exceptions.ResourceNotFoundException;
import com.nanoCurcuminWeb.model.Cart;
import com.nanoCurcuminWeb.model.User;
import com.nanoCurcuminWeb.response.ApiResponse;
import com.nanoCurcuminWeb.service.cart.ICartItemService;
import com.nanoCurcuminWeb.service.cart.ICartService;
import com.nanoCurcuminWeb.service.user.IUserService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/cartItems")
public class CartItemController {
    private final ICartItemService cartItemService;
    private final ICartService cartService;
    private final IUserService userService;


    @PostMapping("/item/add")
    public ResponseEntity<ApiResponse> addItemToCart(@RequestBody Map<String, Object> body) {
        try {
            Long productId = Long.valueOf(body.get("productId").toString());
            Integer quantity = Integer.valueOf(body.get("quantity").toString());
            Long cartId = body.containsKey("cartId") ? Long.valueOf(body.get("cartId").toString()) : null;
            User user = userService.getAuthenticatedUser();
            Cart cart = (cartId != null) ? cartService.getCart(cartId) : cartService.initializeNewCart(user);
            cartItemService.addItemToCart(cart.getId(), productId, quantity);
            return ResponseEntity.ok(new ApiResponse("Item added to cart successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        } catch (JwtException e) {
            return  ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/cart/item/remove")
    public ResponseEntity<ApiResponse> removeItemFromCart(@RequestBody Map<String, Long> body) {
        try {
            Long cartId = body.get("cartId");
            Long itemId = body.get("itemId");
            cartItemService.removeItemFromCart(cartId, itemId);
            return ResponseEntity.ok(new ApiResponse("Remove Item Success", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/cart/item/update")
    public  ResponseEntity<ApiResponse> updateItemQuantity(@RequestBody Map<String, Object> body) {
        try {
            Long cartId = Long.valueOf(body.get("cartId").toString());
            Long itemId = Long.valueOf(body.get("itemId").toString());
            Integer quantity = Integer.valueOf(body.get("quantity").toString());
            cartItemService.updateItemQuantity(cartId, itemId, quantity);
            return ResponseEntity.ok(new ApiResponse("Update Item Success", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }
}
