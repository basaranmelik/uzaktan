package com.guzem.uzaktan.controller.advice;

import com.guzem.uzaktan.dto.response.CartItemResponse;
import com.guzem.uzaktan.dto.response.NotificationResponse;
import com.guzem.uzaktan.dto.response.UserResponse;
import com.guzem.uzaktan.model.common.Role;
import com.guzem.uzaktan.security.CustomUserDetails;
import com.guzem.uzaktan.service.user.CartService;
import com.guzem.uzaktan.service.user.NotificationService;
import com.guzem.uzaktan.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
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
                .anyMatch(a -> Role.ADMIN.getAuthority().equals(a.getAuthority()));
    }

    @ModelAttribute("isTeacher")
    public boolean isTeacher(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) return false;
        return principal.getAuthorities().stream()
                .anyMatch(a -> Role.TEACHER.getAuthority().equals(a.getAuthority()));
    }

    @ModelAttribute("isFirm")
    public boolean isFirm(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) return false;
        return principal.getAuthorities().stream()
                .anyMatch(a -> Role.FIRM.getAuthority().equals(a.getAuthority()));
    }

    @ModelAttribute("cartItems")
    public List<CartItemResponse> cartItems(@ModelAttribute("currentUser") UserResponse currentUser) {
        if (currentUser == null) return List.of();
        try {
            return cartService.getCartItems(currentUser.getId());
        } catch (Exception e) {
            log.warn("Sepet bilgisi alınamadı: {}", e.getMessage());
            return List.of();
        }
    }

    @ModelAttribute("cartCount")
    public int cartCount(@ModelAttribute("currentUser") UserResponse currentUser) {
        if (currentUser == null) return 0;
        try {
            return cartService.getCartCount(currentUser.getId());
        } catch (Exception e) {
            log.warn("Sepet sayısı alınamadı: {}", e.getMessage());
            return 0;
        }
    }

    @ModelAttribute("cartTotal")
    public BigDecimal cartTotal(@ModelAttribute("currentUser") UserResponse currentUser) {
        if (currentUser == null) return BigDecimal.ZERO;
        try {
            return cartService.getCartTotalByUser(currentUser.getId());
        } catch (Exception e) {
            log.warn("Sepet toplamı alınamadı: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    @ModelAttribute("unreadNotifCount")
    public long unreadNotifCount(@ModelAttribute("currentUser") UserResponse currentUser) {
        if (currentUser == null) return 0L;
        try {
            return notificationService.getUnreadCount(currentUser.getId());
        } catch (Exception e) {
            log.warn("Okunmamış bildirim sayısı alınamadı: {}", e.getMessage());
            return 0L;
        }
    }

    @ModelAttribute("recentNotifications")
    public List<NotificationResponse> recentNotifications(@ModelAttribute("currentUser") UserResponse currentUser) {
        if (currentUser == null) return List.of();
        try {
            return notificationService.getRecent(currentUser.getId());
        } catch (Exception e) {
            log.warn("Son bildirimler alınamadı: {}", e.getMessage());
            return List.of();
        }
    }
}
