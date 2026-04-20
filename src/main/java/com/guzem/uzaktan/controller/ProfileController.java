package com.guzem.uzaktan.controller;

import com.guzem.uzaktan.dto.request.ProfileUpdateRequest;
import com.guzem.uzaktan.dto.response.UserResponse;
import com.guzem.uzaktan.service.CertificateService;
import com.guzem.uzaktan.util.PhoneUtils;
import com.guzem.uzaktan.service.EnrollmentService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/profilim")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    private final UserService userService;
    private final CertificateService certificateService;
    private final EnrollmentService enrollmentService;

    @GetMapping
    public String viewProfile(@AuthenticationPrincipal UserDetails principal, Model model) {
        UserResponse user = userService.findByEmail(principal.getUsername());
        model.addAttribute("user", user);
        model.addAttribute("certificates", certificateService.findByUser(user.getId()));
        model.addAttribute("enrollments", enrollmentService.findByUser(user.getId()));
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setFirstName(user.getFirstName());
        request.setLastName(user.getLastName());
        request.setEmail(user.getEmail());
        request.setPhoneNumber(user.getPhoneNumber() != null ? user.getPhoneNumber() : "+90 ");
        request.setBirthDate(user.getBirthDate());
        request.setCity(user.getCity());
        request.setDistrict(user.getDistrict());
        request.setZipCode(user.getZipCode());
        request.setFullAddress(user.getFullAddress());
        model.addAttribute("profileUpdateRequest", request);
        return "profile/view";
    }

    @PostMapping("/guncelle")
    public String updateProfile(@Valid @ModelAttribute("profileUpdateRequest") ProfileUpdateRequest request,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal UserDetails principal,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        UserResponse user = userService.findByEmail(principal.getUsername());
        
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail()) && userService.existsByEmail(request.getEmail())) {
            bindingResult.rejectValue("email", "duplicate", "Bu e-posta adresi zaten kullanımda.");
        }
        
        if (PhoneUtils.isProvided(request.getPhoneNumber())) {
            if (!request.getPhoneNumber().equals(user.getPhoneNumber()) && userService.existsByPhoneNumber(request.getPhoneNumber())) {
                bindingResult.rejectValue("phoneNumber", "duplicate", "Bu telefon numarası zaten kullanımda.");
            }
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("certificates", certificateService.findByUser(user.getId()));
            model.addAttribute("enrollments", enrollmentService.findByUser(user.getId()));
            return "profile/view";
        }

        userService.updateProfile(user.getId(), request);
        redirectAttributes.addFlashAttribute("successMessage", "Profiliniz güncellendi.");
        return "redirect:/profilim";
    }
}
