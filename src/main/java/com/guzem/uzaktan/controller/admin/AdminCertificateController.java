package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.dto.response.ActionResult;
import com.guzem.uzaktan.service.course.CertificateService;
import com.guzem.uzaktan.service.course.CourseService;
import com.guzem.uzaktan.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
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
    public ActionResult issueCertificate(@RequestParam Long userId, @RequestParam Long courseId) {
        try {
            certificateService.issueCertificate(userId, courseId);
            return ActionResult.success("Sertifika başarıyla oluşturuldu.", "/admin/sertifikalar");
        } catch (Exception e) {
            log.error("Sertifika oluşturma hatası: {}", e.getMessage(), e);
            return ActionResult.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/iptal")
    public ActionResult revokeCertificate(@PathVariable Long id) {
        try {
            certificateService.revoke(id);
            return ActionResult.success("Sertifika iptal edildi.", "/admin/sertifikalar");
        } catch (Exception e) {
            log.error("Sertifika iptal hatası: {}", e.getMessage(), e);
            return ActionResult.error(e.getMessage());
        }
    }
}
