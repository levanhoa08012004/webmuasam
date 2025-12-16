package com.example.webmuasam.controller;

import java.util.List;

import com.example.webmuasam.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.webmuasam.dto.Request.CreateCartItemRequest;
import com.example.webmuasam.dto.Response.CartItemResponse;
import com.example.webmuasam.dto.Response.CartResponse;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.service.CartItemService;
import com.example.webmuasam.service.CartService;

@RestController
@RequestMapping("/api/v1/cartitems")
@RequiredArgsConstructor
@Validated
public class CartItemController {
    private final CartItemService cartItemService;
    private final CartService cartService;


    @ApiMessage("get all cart item by cart Id")
    @GetMapping("/{id}")
    public ResponseEntity<List<CartItemResponse>> getAllCartItemByCartId(@PathVariable Long id) throws AppException {
        return ResponseEntity.ok(this.cartItemService.getAllCartItemsByCartId(id));
    }

    @ApiMessage("get cart by current User")
    @GetMapping("/cart")
    public ResponseEntity<CartResponse> getCart() throws AppException {
        return ResponseEntity.ok(this.cartService.getCartByCurrentUser());
    }

    @ApiMessage("add cart item to cart")
    @PostMapping
    public ResponseEntity<CartItemResponse> addToCart(@Valid @RequestBody CreateCartItemRequest request) throws AppException {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.cartItemService.addCartItem(request));
    }

    @PutMapping("/incre/{id}")
    public ResponseEntity<CartItemResponse> increCartItem(@PathVariable Long id) throws AppException {
        return ResponseEntity.status(HttpStatus.OK).body(this.cartItemService.incrementCartItem(id));
    }

    @PutMapping("/des/{id}")
    public ResponseEntity<CartItemResponse> desCartItem(@PathVariable Long id) throws AppException {
        return ResponseEntity.status(HttpStatus.OK).body(this.cartItemService.decrementCartItem(id));
    }

    @ApiMessage("update cart item")
    @PutMapping("/update")
    public ResponseEntity<CartItemResponse> updateCartItem(
            @RequestParam Long cartItemId, @RequestParam Integer quantity) throws AppException {
        return ResponseEntity.status(HttpStatus.OK).body(this.cartItemService.updateCartItem(cartItemId, quantity));
    }

    @ApiMessage("delete cart item by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCartItem(@PathVariable Long id) throws AppException {
        this.cartItemService.deleteCartItem(id);
        return ResponseEntity.ok("delete cartItem is success");
    }
}
