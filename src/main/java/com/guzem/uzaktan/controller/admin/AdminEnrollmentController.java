package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.service.course.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.data.domain.Page;
import com.guzem.uzaktan.dto.response.EnrollmentResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        Page<EnrollmentResponse> enrollments = enrollmentService.findAllForAdmin(page, size);

        Map<String, List<EnrollmentResponse>> groupedEnrollments = enrollments.getContent().stream()
                .collect(Collectors.groupingBy(
                        EnrollmentResponse::getCourseTitle,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        model.addAttribute("enrollments", enrollments);
        model.addAttribute("groupedEnrollments", groupedEnrollments);
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
