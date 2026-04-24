package com.guzem.uzaktan.controller.user;

import com.guzem.uzaktan.service.user.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping
    public String viewCart() {
        return "cart/index";
    }

    @PostMapping("/ekle")
    public String addToCart(@RequestParam Long courseId,
                            @ModelAttribute("currentUserId") Long currentUserId,
                            RedirectAttributes ra) {
        try {
            cartService.addToCart(currentUserId, courseId);
            ra.addFlashAttribute("successMessage", "Kurs sepete eklendi.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/egitimler/" + courseId;
    }

    @PostMapping("/kaldir")
    public String removeFromCart(@RequestParam Long courseId,
                                 @RequestParam(defaultValue = "false") boolean fromCart,
                                 @ModelAttribute("currentUserId") Long currentUserId,
                                 RedirectAttributes ra) {
        cartService.removeFromCart(currentUserId, courseId);
        ra.addFlashAttribute("successMessage", "Kurs sepetten kaldırıldı.");
        if (fromCart) {
            return "redirect:/sepet";
        }
        return "redirect:/egitimler/" + courseId;
    }

    @PostMapping(value = "/kaldir-ajax", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeAjax(@RequestParam Long courseId,
                                                          @ModelAttribute("currentUserId") Long currentUserId) {
        cartService.removeFromCart(currentUserId, courseId);
        int newCount = cartService.getCartCount(currentUserId);
        BigDecimal newTotal = cartService.getCartTotalByUser(currentUserId);
        return ResponseEntity.ok(Map.of("count", newCount, "total", newTotal));
    }

    @PostMapping("/checkout")
    public String checkoutCart(@ModelAttribute("currentUserId") Long currentUserId, RedirectAttributes ra) {
        cartService.checkout(currentUserId);
        ra.addFlashAttribute("successMessage", "Sepetteki eğitimlere kayıt talebi oluşturuldu. Yönetici onayı bekleniyor.");
        return "redirect:/panom";
    }
}
