package com.example.webmuasam.controller;

import com.example.webmuasam.dto.Request.CreateCartItemRequest;
import com.example.webmuasam.dto.Response.CartItemResponse;
import com.example.webmuasam.dto.Response.CartResponse;
import com.example.webmuasam.entity.Cart;
import com.example.webmuasam.entity.CartItem;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.service.CartItemService;
import com.example.webmuasam.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cartitems")
public class CartItemController {
    private final CartItemService cartItemService;
    private final CartService cartService;
    public CartItemController(CartItemService cartItemService, CartService cartService) {
        this.cartItemService = cartItemService;
        this.cartService = cartService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<CartItemResponse>> getAllCartItemByCartId(@PathVariable Long id) throws AppException {
        return ResponseEntity.ok(this.cartItemService.getAllCartItemByCartId(id));
    }

    @GetMapping("/cart")
    public ResponseEntity<CartResponse> getCart() throws AppException {
        return ResponseEntity.ok(this.cartService.getCartByCurrentUser());
    }

    @PostMapping
    public ResponseEntity<CartItemResponse> addToCart(@RequestBody CreateCartItemRequest request) throws AppException {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.cartItemService.addCartItem(request));
    }

    @PutMapping("/incre/{id}")
    public ResponseEntity<CartItemResponse> increCartItem(@PathVariable Long id) throws AppException {
        return ResponseEntity.status(HttpStatus.OK).body(this.cartItemService.increCartItem(id));
    }
    @PutMapping("/des/{id}")
    public ResponseEntity<CartItemResponse> desCartItem(@PathVariable Long id) throws AppException {
        return ResponseEntity.status(HttpStatus.OK).body(this.cartItemService.desCartItem(id));
    }
    @PutMapping("/update")
    public ResponseEntity<CartItemResponse> updateCartItem(@RequestParam Long cartItemId,
                                                           @RequestParam Integer quantity) throws AppException {
        return ResponseEntity.status(HttpStatus.OK).body(this.cartItemService.updateCartItem(cartItemId, quantity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCartItem(@PathVariable Long id) throws AppException {
        this.cartItemService.deleteCartItem(id);
        return ResponseEntity.ok("delete cartItem is success");
    }



}
