package com.guzem.uzaktan.controller.course;

import com.guzem.uzaktan.service.course.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/kayitlarim")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    public String enroll(@RequestParam Long courseId,
                         @ModelAttribute("currentUserId") Long currentUserId,
                         RedirectAttributes redirectAttributes) {
        enrollmentService.enroll(currentUserId, courseId);
        redirectAttributes.addFlashAttribute("successMessage", "Kursa başarıyla kaydoldunuz!");
        return "redirect:/egitimler/" + courseId;
    }


    
}
