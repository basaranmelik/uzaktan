package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/kayitlar")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminEnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping
    public String enrollments(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "25") int size,
                              Model model) {
        model.addAttribute("enrollments", enrollmentService.findAllForAdmin(page, size));
        return "admin/enrollments";
    }

    @PostMapping("/{id}/aktifle")
    public String activateEnrollment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        enrollmentService.activateEnrollment(id);
        redirectAttributes.addFlashAttribute("successMessage", "Kayıt aktifleştirildi.");
        return "redirect:/admin/kayitlar";
    }

    @PostMapping("/{id}/sil")
    public String deleteEnrollment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        enrollmentService.deleteEnrollment(id);
        redirectAttributes.addFlashAttribute("successMessage", "Kayıt silindi.");
        return "redirect:/admin/kayitlar";
    }
}
