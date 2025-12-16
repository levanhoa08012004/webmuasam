package com.example.webmuasam.service;

import java.util.List;
import java.util.Optional;

import com.example.webmuasam.dto.Request.CreateCartItemRequest;
import com.example.webmuasam.dto.Response.CartItemResponse;
import com.example.webmuasam.entity.Cart;
import com.example.webmuasam.entity.CartItem;
import com.example.webmuasam.entity.Product;
import com.example.webmuasam.entity.ProductVariant;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.repository.CartItemRepository;
import com.example.webmuasam.repository.CartRepository;
import com.example.webmuasam.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final ProductVariantRepository productVariantRepository;



    private CartItemResponse convertToResponse(CartItem cartItem) {
        ProductVariant variant = cartItem.getProductVariant();
        Product product = variant.getProduct();

        CartItemResponse.ProductInfo productInfo =
                new CartItemResponse.ProductInfo(product.getId(), product.getName(), product.getPrice());

        CartItemResponse.ProductVariantInfo variantInfo =
                new CartItemResponse.ProductVariantInfo(variant.getId(), variant.getColor(), variant.getSize(), productInfo);

        CartItemResponse response = new CartItemResponse();
        response.setId(cartItem.getId());
        response.setQuantity(cartItem.getQuantity());
        response.setPrice(cartItem.getPrice());
        response.setProductVariant(variantInfo);

        return response;
    }

    @Transactional(rollbackFor = AppException.class)
    public CartItemResponse addCartItem(CreateCartItemRequest request) throws AppException {
        Cart cart = cartRepository.findById(request.getCartId())
                .orElseThrow(() -> new AppException("Cart does not exist"));
        ProductVariant variant = productVariantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new AppException("Product variant does not exist"));

        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProductVariant(cart, variant);
        CartItem cartItem;

        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            if (newQuantity > variant.getStockQuantity()) {
                throw new AppException("Not enough stock. Only " + variant.getStockQuantity() + " left.");
            }
            cartItem.setQuantity(newQuantity);
        } else {
            if (request.getQuantity() > variant.getStockQuantity()) {
                throw new AppException("Not enough stock. Only " + variant.getStockQuantity() + " left.");
            }
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProductVariant(variant);
            cartItem.setQuantity(request.getQuantity());
        }

        cartItem.setPrice(variant.getProduct().getPrice() * cartItem.getQuantity());
        return convertToResponse(cartItemRepository.save(cartItem));
    }

    public CartItemResponse incrementCartItem(Long cartItemId) throws AppException {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new AppException("Cart item does not exist"));
        int newQuantity = cartItem.getQuantity() + 1;
        ProductVariant variant = cartItem.getProductVariant();

        if (newQuantity > variant.getStockQuantity()) {
            throw new AppException("Not enough stock. Only " + variant.getStockQuantity() + " left.");
        }

        cartItem.setQuantity(newQuantity);
        cartItem.setPrice(variant.getProduct().getPrice() * newQuantity);
        return convertToResponse(cartItemRepository.save(cartItem));
    }

    public CartItemResponse updateCartItem(Long cartItemId, Integer quantity) throws AppException {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new AppException("Cart item does not exist"));

        if (quantity <= 0) {
            deleteCartItem(cartItemId);
            return null;
        }

        ProductVariant variant = cartItem.getProductVariant();
        if (quantity > variant.getStockQuantity()) {
            throw new AppException("Not enough stock. Only " + variant.getStockQuantity() + " left.");
        }

        cartItem.setQuantity(quantity);
        cartItem.setPrice(variant.getProduct().getPrice() * quantity);
        return convertToResponse(cartItemRepository.save(cartItem));
    }

    @Transactional
    public void deleteCartItem(Long cartItemId) throws AppException {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new AppException("Cart item does not exist"));
        cartItemRepository.delete(cartItem);
    }
    @Transactional
    public CartItemResponse decrementCartItem(Long cartItemId) throws AppException {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new AppException("Cart item does not exist"));

        int newQuantity = cartItem.getQuantity() - 1;
        if (newQuantity <= 0) {
            deleteCartItem(cartItemId);
            return null;
        }

        cartItem.setQuantity(newQuantity);
        cartItem.setPrice(cartItem.getProductVariant().getProduct().getPrice() * newQuantity);
        return convertToResponse(cartItemRepository.save(cartItem));
    }

    public List<CartItemResponse> getAllCartItemsByCartId(Long cartId) throws AppException {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new AppException("Cart does not exist"));

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        return cartItems.stream()
                .map(this::convertToResponse)
                .toList();
    }
}
