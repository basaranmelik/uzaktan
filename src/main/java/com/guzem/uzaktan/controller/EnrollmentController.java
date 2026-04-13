package com.guzem.uzaktan.controller;

import com.guzem.uzaktan.dto.response.UserResponse;
import com.guzem.uzaktan.service.EnrollmentService;
import com.guzem.uzaktan.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/kayitlarim")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final UserService userService;

    @PostMapping
    public String enroll(@RequestParam Long courseId,
                         @AuthenticationPrincipal UserDetails principal,
                         RedirectAttributes redirectAttributes) {
        UserResponse user = userService.findByEmail(principal.getUsername());
        enrollmentService.enroll(user.getId(), courseId);
        redirectAttributes.addFlashAttribute("successMessage", "Kursa başarıyla kaydoldunuz!");
        return "redirect:/egitimler/" + courseId;
    }

    @PostMapping("/{id}/birak")
    public String drop(@PathVariable Long id,
                       @AuthenticationPrincipal UserDetails principal,
                       RedirectAttributes redirectAttributes) {
        UserResponse user = userService.findByEmail(principal.getUsername());
        enrollmentService.drop(id, user.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Kurs kaydınız iptal edildi.");
        return "redirect:/panom";
    }
    
}
