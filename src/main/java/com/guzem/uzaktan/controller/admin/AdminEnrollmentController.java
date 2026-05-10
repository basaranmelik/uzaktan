package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.dto.response.ActionResult;
import com.guzem.uzaktan.dto.response.EnrollmentResponse;
import com.guzem.uzaktan.service.course.CourseService;
import com.guzem.uzaktan.service.course.EnrollmentExcelExportService;
import com.guzem.uzaktan.service.course.EnrollmentService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/admin/kayitlar")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminEnrollmentController {

    private final EnrollmentService enrollmentService;
    private final EnrollmentExcelExportService enrollmentExcelExportService;
    private final CourseService courseService;

    @GetMapping
    public String enrollments(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "25") int size,
                              Model model) {
        Page<EnrollmentResponse> enrollments = enrollmentService.findAllForAdmin(page, size);
        Map<String, List<EnrollmentResponse>> groupedEnrollments = enrollments.getContent().stream()
                .collect(Collectors.groupingBy(EnrollmentResponse::getCourseTitle, LinkedHashMap::new, Collectors.toList()));
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("groupedEnrollments", groupedEnrollments);
        return "admin/enrollments";
    }

    @PostMapping("/{id}/aktifle")
    public ActionResult activateEnrollment(@PathVariable Long id) {
        try {
            enrollmentService.activateEnrollment(id);
            return ActionResult.success("Kayıt aktifleştirildi.", "/admin/kayitlar");
        } catch (Exception e) {
            log.error("Kayıt aktifleştirme hatası: {}", e.getMessage(), e);
            return ActionResult.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/sil")
    public ActionResult deleteEnrollment(@PathVariable Long id) {
        try {
            enrollmentService.deleteEnrollment(id);
            return ActionResult.success("Kayıt silindi.", "/admin/kayitlar");
        } catch (Exception e) {
            log.error("Kayıt silme hatası: {}", e.getMessage(), e);
            return ActionResult.error(e.getMessage());
        }
    }

    @GetMapping("/export-excel")
    public void exportAllExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"tum-kayitlar.xlsx\"");
        enrollmentExcelExportService.exportAllToExcel(response.getOutputStream());
    }

    @GetMapping("/kurs/{courseId}/export-excel")
    public void exportByCourseExcel(@PathVariable Long courseId, HttpServletResponse response) throws IOException {
        String filename = "kayitlar-" + courseService.findById(courseId).getTitle()
                .replaceAll("[^a-zA-Z0-9ğüşöçıİĞÜŞÖÇ\\s]", "").trim().replace(' ', '_') + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        enrollmentExcelExportService.exportByCourseToExcel(courseId, response.getOutputStream());
    }
}
