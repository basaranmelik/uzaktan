package com.guzem.uzaktan.controller;

import com.guzem.uzaktan.dto.request.RegisterRequest;
import com.guzem.uzaktan.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/giris")
    public String loginForm(@AuthenticationPrincipal UserDetails principal) {
        if (principal != null) return "redirect:/panom";
        return "auth/login";
    }

    @GetMapping("/kayit-ol")
    public String registerForm(@AuthenticationPrincipal UserDetails principal, Model model) {
        if (principal != null) return "redirect:/panom";
        RegisterRequest req = new RegisterRequest();
        req.setPhoneNumber("+90 ");
        model.addAttribute("registerRequest", req);
        return "auth/register";
    }

    @PostMapping("/kayit-ol")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                           BindingResult bindingResult,
                           Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        if (userService.existsByEmail(request.getEmail())) {
            bindingResult.rejectValue("email", "duplicate", "Bu e-posta adresi zaten kullanımda.");
            return "auth/register";
        }
        
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty() && !request.getPhoneNumber().equals("+90 ")) {
            if (userService.existsByPhoneNumber(request.getPhoneNumber())) {
                bindingResult.rejectValue("phoneNumber", "duplicate", "Bu telefon numarası zaten kullanımda.");
                return "auth/register";
            }
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "Şifreler eşleşmiyor.");
            return "auth/register";
        }

        userService.register(request);
        return "redirect:/giris?kayit=basarili";
    }
}
