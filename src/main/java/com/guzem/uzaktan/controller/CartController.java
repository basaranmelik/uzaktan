package com.guzem.uzaktan.controller;

import com.guzem.uzaktan.service.CartService;
import com.guzem.uzaktan.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@RequestMapping("/sepet")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    @GetMapping
    public String viewCart() {
        return "cart/index";
    }

    @PostMapping("/ekle")
    public String addToCart(@RequestParam Long courseId,
                            @AuthenticationPrincipal UserDetails principal,
                            RedirectAttributes ra) {
        try {
            Long userId = userService.findUserIdByEmail(principal.getUsername());
            cartService.addToCart(userId, courseId);
            ra.addFlashAttribute("successMessage", "Kurs sepete eklendi.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/egitimler/" + courseId;
    }

    @PostMapping("/kaldir")
    public String removeFromCart(@RequestParam Long courseId,
                                 @RequestParam(defaultValue = "false") boolean fromCart,
                                 @AuthenticationPrincipal UserDetails principal,
                                 RedirectAttributes ra) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        cartService.removeFromCart(userId, courseId);
        ra.addFlashAttribute("successMessage", "Kurs sepetten kaldırıldı.");
        if (fromCart) {
            return "redirect:/sepet";
        }
        return "redirect:/egitimler/" + courseId;
    }

    @PostMapping(value = "/kaldir-ajax", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeAjax(@RequestParam Long courseId,
                                                          @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        cartService.removeFromCart(userId, courseId);
        int newCount = cartService.getCartCount(userId);
        BigDecimal newTotal = cartService.getCartTotalByUser(userId);
        return ResponseEntity.ok(Map.of("count", newCount, "total", newTotal));
    }

    @PostMapping("/checkout")
    public String checkoutCart(@AuthenticationPrincipal UserDetails principal, RedirectAttributes ra) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        cartService.checkout(userId);
        ra.addFlashAttribute("successMessage", "Sepetteki eğitimlere kayıt talebi oluşturuldu. Yönetici onayı bekleniyor.");
        return "redirect:/panom";
    }
}
