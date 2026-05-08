package com.guzem.uzaktan.controller.advice;

import com.guzem.uzaktan.dto.response.CartItemResponse;
import com.guzem.uzaktan.dto.response.NotificationResponse;
import com.guzem.uzaktan.dto.response.UserResponse;
import com.guzem.uzaktan.security.CustomUserDetails;
import com.guzem.uzaktan.service.user.CartService;
import com.guzem.uzaktan.service.user.NotificationService;
import com.guzem.uzaktan.service.user.UserService;
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
    private final NotificationService notificationService;

    /**
     * Single DB call per request — all other model attributes derive from this.
     */
    @ModelAttribute("currentUser")
    public UserResponse currentUser(@AuthenticationPrincipal UserDetails principal) {
        if (principal instanceof CustomUserDetails details) {
            return userService.findById(details.getUserId());
        }
        return null;
    }

    @ModelAttribute("currentUserId")
    public Long currentUserId(@ModelAttribute("currentUser") UserResponse currentUser) {
        return currentUser != null ? currentUser.getId() : null;
    }

    @ModelAttribute("currentUserFullName")
    public String currentUserFullName(@ModelAttribute("currentUser") UserResponse currentUser) {
        return currentUser != null ? currentUser.getFullName() : null;
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) return false;
        return principal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    @ModelAttribute("isTeacher")
    public boolean isTeacher(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) return false;
        return principal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_TEACHER".equals(a.getAuthority()));
    }

    @ModelAttribute("isFirm")
    public boolean isFirm(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) return false;
        return principal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_FIRM".equals(a.getAuthority()));
    }

    @ModelAttribute("cartItems")
    public List<CartItemResponse> cartItems(@ModelAttribute("currentUser") UserResponse currentUser) {
        if (currentUser == null) return List.of();
        return cartService.getCartItems(currentUser.getId());
    }

    @ModelAttribute("cartCount")
    public int cartCount(@ModelAttribute("currentUser") UserResponse currentUser) {
        if (currentUser == null) return 0;
        return cartService.getCartCount(currentUser.getId());
    }

    @ModelAttribute("cartTotal")
    public BigDecimal cartTotal(@ModelAttribute("currentUser") UserResponse currentUser) {
        if (currentUser == null) return BigDecimal.ZERO;
        return cartService.getCartTotalByUser(currentUser.getId());
    }

    @ModelAttribute("unreadNotifCount")
    public long unreadNotifCount(@ModelAttribute("currentUser") UserResponse currentUser) {
        if (currentUser == null) return 0L;
        return notificationService.getUnreadCount(currentUser.getId());
    }

    @ModelAttribute("recentNotifications")
    public List<NotificationResponse> recentNotifications(@ModelAttribute("currentUser") UserResponse currentUser) {
        if (currentUser == null) return List.of();
        return notificationService.getRecent(currentUser.getId());
    }
}
