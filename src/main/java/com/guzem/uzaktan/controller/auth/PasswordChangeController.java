package com.guzem.uzaktan.controller.auth;

import com.guzem.uzaktan.model.common.Role;
import com.guzem.uzaktan.security.CustomUserDetails;
import com.guzem.uzaktan.security.CustomUserDetailsService;
import com.guzem.uzaktan.service.common.PasswordPolicy;
import com.guzem.uzaktan.service.user.PasswordChangeRateLimitService;
import com.guzem.uzaktan.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PasswordChangeController {

    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordChangeRateLimitService rateLimitService;

    @GetMapping("/sifre-degistir")
    public String showForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getPrincipal() instanceof CustomUserDetails details) {
            model.addAttribute("forced", details.isPasswordResetRequired());
        }
        return "auth/sifre-degistir";
    }

    @PostMapping("/sifre-degistir")
    public String changePassword(@RequestParam(required = false) String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = ((CustomUserDetails) auth.getPrincipal()).getUsername();

        if (!rateLimitService.tryConsume(userId)) {
            model.addAttribute("error", "Çok fazla deneme yaptınız. Lütfen 15 dakika sonra tekrar deneyin.");
            return "auth/sifre-degistir";
        }

        if (newPassword == null || !PasswordPolicy.PATTERN.matcher(newPassword).matches()) {
            model.addAttribute("error", PasswordPolicy.MESSAGE);
            return "auth/sifre-degistir";
        }
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Şifreler eşleşmiyor.");
            return "auth/sifre-degistir";
        }

        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();

        if (details.isPasswordResetRequired()) {
            userService.forceChangePassword(details.getUserId(), newPassword);
        } else {
            if (currentPassword == null || currentPassword.isBlank()) {
                model.addAttribute("error", "Mevcut şifrenizi girmeniz gerekiyor.");
                return "auth/sifre-degistir";
            }
            try {
                userService.changePassword(details.getUserId(), currentPassword, newPassword);
            } catch (IllegalArgumentException e) {
                model.addAttribute("error", e.getMessage());
                return "auth/sifre-degistir";
            }
        }

        UserDetails refreshed = userDetailsService.loadUserByUsername(details.getUsername());
        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                refreshed, refreshed.getPassword(), refreshed.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        if (hasRole(auth, Role.ADMIN)) return "redirect:/admin";
        if (hasRole(auth, Role.FIRM)) return "redirect:/admin/kurslar";
        if (hasRole(auth, Role.TEACHER)) return "redirect:/egitmen/panel";
        return "redirect:/panom";
    }

    private static boolean hasRole(Authentication auth, Role role) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role.getAuthority()));
    }
}
