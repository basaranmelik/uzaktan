package com.guzem.uzaktan.controller;

import com.guzem.uzaktan.dto.response.CartItemResponse;
import com.guzem.uzaktan.service.CartService;
import com.guzem.uzaktan.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.math.BigDecimal;
import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final UserService userService;
    private final CartService cartService;

    @ModelAttribute("currentUserFullName")
    public String currentUserFullName(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) return null;
        return userService.findByEmail(principal.getUsername()).getFullName();
    }

    @ModelAttribute("cartItems")
    public List<CartItemResponse> cartItems(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) return List.of();
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        return cartService.getCartItems(userId);
    }

    @ModelAttribute("cartCount")
    public int cartCount(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) return 0;
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        return cartService.getCartCount(userId);
    }

    @ModelAttribute("cartTotal")
    public BigDecimal cartTotal(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) return BigDecimal.ZERO;
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        return cartService.getCartTotalByUser(userId);
    }
}
