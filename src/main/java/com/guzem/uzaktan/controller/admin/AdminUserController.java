package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.model.Role;
import com.guzem.uzaktan.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/kullanicilar")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public String users(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("roles", Role.values());
        return "admin/users";
    }

    @PostMapping("/{id}/kilitle")
    public String toggleLock(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails principal,
                             RedirectAttributes redirectAttributes) {
        Long currentUserId = userService.findUserIdByEmail(principal.getUsername());
        if (currentUserId.equals(id)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Kendi hesabınızı kilitleyemezsiniz.");
            return "redirect:/admin/kullanicilar";
        }
        userService.toggleUserLock(id);
        redirectAttributes.addFlashAttribute("successMessage", "Kullanıcı durumu güncellendi.");
        return "redirect:/admin/kullanicilar";
    }

    @PostMapping("/{id}/rol")
    public String changeRole(@PathVariable("id") Long id,
                             @RequestParam("role") Role role,
                             @AuthenticationPrincipal UserDetails principal,
                             RedirectAttributes redirectAttributes) {
        Long currentUserId = userService.findUserIdByEmail(principal.getUsername());
        if (currentUserId.equals(id)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Kendi rolünüzü değiştiremezsiniz.");
            return "redirect:/admin/kullanicilar";
        }
        userService.changeRole(id, role);
        redirectAttributes.addFlashAttribute("successMessage", "Kullanıcı rolü güncellendi.");
        return "redirect:/admin/kullanicilar";
    }

    @PostMapping("/{id}/sil")
    public String deleteUser(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails principal,
                             RedirectAttributes redirectAttributes) {
        Long currentUserId = userService.findUserIdByEmail(principal.getUsername());
        if (currentUserId.equals(id)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Kendi hesabınızı silemezsiniz.");
            return "redirect:/admin/kullanicilar";
        }
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("successMessage", "Kullanıcı silindi.");
        return "redirect:/admin/kullanicilar";
    }
}
