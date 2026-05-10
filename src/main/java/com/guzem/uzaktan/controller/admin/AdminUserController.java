package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.dto.response.ActionResult;
import com.guzem.uzaktan.model.common.Role;
import com.guzem.uzaktan.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
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
    public ActionResult toggleLock(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails principal) {
        Long currentUserId = userService.findUserIdByEmail(principal.getUsername());
        if (currentUserId.equals(id)) {
            return ActionResult.error("Kendi hesabınızı kilitleyemezsiniz.");
        }
        try {
            userService.toggleUserLock(id);
            return ActionResult.success("Kullanıcı durumu güncellendi.", "/admin/kullanicilar");
        } catch (Exception e) {
            log.error("Kullanıcı kilitleme hatası: {}", e.getMessage(), e);
            return ActionResult.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/rol")
    public ActionResult changeRole(@PathVariable Long id,
                                   @RequestParam Role role,
                                   @AuthenticationPrincipal UserDetails principal) {
        Long currentUserId = userService.findUserIdByEmail(principal.getUsername());
        if (currentUserId.equals(id)) {
            return ActionResult.error("Kendi rolünüzü değiştiremezsiniz.");
        }
        try {
            userService.changeRole(id, role);
            return ActionResult.success("Rol güncellendi.", "/admin/kullanicilar");
        } catch (Exception e) {
            log.error("Rol değiştirme hatası: {}", e.getMessage(), e);
            return ActionResult.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/sil")
    public ActionResult deleteUser(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails principal) {
        Long currentUserId = userService.findUserIdByEmail(principal.getUsername());
        if (currentUserId.equals(id)) {
            return ActionResult.error("Kendi hesabınızı silemezsiniz.");
        }
        try {
            userService.deleteUser(id);
            return ActionResult.success("Kullanıcı silindi.", "/admin/kullanicilar");
        } catch (Exception e) {
            log.error("Kullanıcı silme hatası: {}", e.getMessage(), e);
            return ActionResult.error(e.getMessage());
        }
    }
}
