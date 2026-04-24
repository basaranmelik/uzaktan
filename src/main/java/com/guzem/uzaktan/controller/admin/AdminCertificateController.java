package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.service.course.CertificateService;
import com.guzem.uzaktan.service.course.CourseService;
import com.guzem.uzaktan.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/sertifikalar")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCertificateController {

    private final CertificateService certificateService;
    private final UserService userService;
    private final CourseService courseService;

    @GetMapping
    public String certificates(Model model) {
        model.addAttribute("certificates", certificateService.findAll());
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("courses", courseService.findAllForAdmin(0, 200).getContent());
        return "admin/certificates";
    }

    @PostMapping("/ver")
    public String issueCertificate(@RequestParam("userId") Long userId,
                                   @RequestParam("courseId") Long courseId,
                                   RedirectAttributes redirectAttributes) {
        certificateService.issueCertificate(userId, courseId);
        redirectAttributes.addFlashAttribute("successMessage", "Sertifika düzenlendi.");
        return "redirect:/admin/sertifikalar";
    }

    @PostMapping("/{id}/iptal")
    public String revokeCertificate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        certificateService.revoke(id);
        redirectAttributes.addFlashAttribute("successMessage", "Sertifika iptal edildi.");
        return "redirect:/admin/sertifikalar";
    }
}
