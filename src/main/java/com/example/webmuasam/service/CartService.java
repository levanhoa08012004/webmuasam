package com.example.webmuasam.service;

import com.example.webmuasam.dto.Response.CartResponse;
import com.example.webmuasam.entity.Cart;
import com.example.webmuasam.entity.CartItem;
import com.example.webmuasam.entity.User;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.repository.CartItemRepository;
import com.example.webmuasam.repository.CartRepository;
import com.example.webmuasam.repository.UserRepository;
import com.example.webmuasam.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;

    public CartService(CartRepository cartRepository, UserRepository userRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public Cart createCart(Long userId) throws AppException {
        if (userId == null) {
            throw new AppException("User ID không được null");
        }

        return this.cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = null;
            try {
                user = this.userRepository.findById(userId)
                        .orElseThrow(() -> new AppException("User not found"));
            } catch (AppException e) {
                throw new RuntimeException(e);
            }

            Cart cart = new Cart();
            cart.setUser(user);
            return cartRepository.save(cart);
        });
    }


    public CartResponse getCartByCurrentUser()throws AppException {
        String email = SecurityUtil.getCurrentUserLogin().orElseThrow(()->new AppException("chưa đăng nhập"));

        User user = this.userRepository.findByEmail(email).orElseThrow(()->new AppException("người dùng không tồn tại"));
        Cart cart = this.cartRepository.findByUserId(user.getId()).orElseGet(()->{
            Cart newCart = new Cart();
            newCart.setUser(user);
            return this.cartRepository.save(newCart);
        });

        double totalPrice = cart.getCartItems()==null ?0.0: cart.getCartItems().stream()
                .mapToDouble(CartItem->{
                    return CartItem.getPrice()*CartItem.getQuantity();
                })
                .sum();
        int totalQuantity = cart.getCartItems()==null ?0:cart.getCartItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
        CartResponse cartResponse = new CartResponse();
        cartResponse.setId(cart.getId());
        cartResponse.setTotalPrice(totalPrice);
        cartResponse.setQuantityTotal(totalQuantity);
        List<CartResponse.Item> items = cart.getCartItems()==null ?null:cart.getCartItems().stream().map(i->{
            CartResponse.Item item = new CartResponse.Item();
            CartResponse.ProductVariantCart productVariant = new CartResponse.ProductVariantCart();
            productVariant.setId(i.getProductVariant().getId());
            productVariant.setColor(i.getProductVariant().getColor());
            productVariant.setSize(i.getProductVariant().getSize());
            productVariant.setStockQuantity(i.getProductVariant().getStockQuantity());
            item.setId(i.getId());
            item.setPrice(i.getPrice());
            item.setQuantity(i.getQuantity());
            item.setProductVariant(productVariant);
            item.setName(i.getProductVariant().getProduct().getName());
            item.setImage(Base64.getEncoder().encodeToString(i.getProductVariant().getProduct().getImages().getFirst().getBaseImage()));
            return item;
        }).collect(Collectors.toList());
        cartResponse.setCartItems(items);

        return cartResponse;
    }

    public void clearCart(Long cartId)throws AppException {
        Cart cart = this.cartRepository.findById(cartId).orElseThrow(()->new AppException("cart id không tồn tại"));
        this.cartItemRepository.deleteAll(cart.getCartItems());
    }



    @Transactional
    public void clearCartByUserId(Long userId) throws AppException{
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException("Không tìm thấy giỏ hàng"));
        cartItemRepository.deleteByCartId(cart.getId());
    }



}
