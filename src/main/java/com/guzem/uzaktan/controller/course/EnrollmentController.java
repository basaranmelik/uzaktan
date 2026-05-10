package com.guzem.uzaktan.controller.course;

import com.guzem.uzaktan.service.course.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/egitimler/{id}/kaydol")
    public String enroll(@PathVariable Long id,
                         @ModelAttribute("currentUserId") Long currentUserId,
                         RedirectAttributes redirectAttributes) {
        enrollmentService.enroll(currentUserId, id);
        redirectAttributes.addFlashAttribute("successMessage", "Kursa başarıyla kaydoldunuz!");
        return "redirect:/egitimler/" + id;
    }
}
