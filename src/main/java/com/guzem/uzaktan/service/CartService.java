package com.guzem.uzaktan.service;

import com.guzem.uzaktan.dto.response.CartItemResponse;

import java.math.BigDecimal;
import java.util.List;

public interface CartService {

    void addToCart(Long userId, Long courseId);

    void removeFromCart(Long userId, Long courseId);

    void clearCart(Long userId);

    List<CartItemResponse> getCartItems(Long userId);

    int getCartCount(Long userId);

    boolean isInCart(Long userId, Long courseId);

    BigDecimal getCartTotal(List<CartItemResponse> items);

    BigDecimal getCartTotalByUser(Long userId);

    void checkout(Long userId);
}
