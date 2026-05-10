package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.dto.response.ActionResult;
import com.guzem.uzaktan.dto.request.CourseCreateRequest;
import com.guzem.uzaktan.dto.request.CourseUpdateRequest;
import com.guzem.uzaktan.dto.response.CourseResponse;
import com.guzem.uzaktan.dto.response.InstructorResponse;
import com.guzem.uzaktan.model.course.CourseStatus;
import com.guzem.uzaktan.service.course.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/kurslar")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'FIRM')")
public class AdminCourseController {

    private final CourseService courseService;

    @GetMapping
    public String courses(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "20") int size,
                          Model model) {
        model.addAttribute("courses", courseService.findAllForAdmin(page, size));
        model.addAttribute("statuses", CourseStatus.values());
        return "admin/courses";
    }

    @GetMapping("/yeni")
    public String newCourseForm(Model model) {
        model.addAttribute("courseCreateRequest", new CourseCreateRequest());
        return "admin/course-form";
    }

    @PostMapping
    public String createCourse(@Valid @ModelAttribute("courseCreateRequest") CourseCreateRequest request,
                               BindingResult bindingResult,
                               @RequestParam(value = "image", required = false) MultipartFile image,
                               @ModelAttribute("currentUserId") Long currentUserId,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/course-form";
        }
        try {
            courseService.create(request, image, currentUserId);
            redirectAttributes.addFlashAttribute("successMessage", "Kurs başarıyla oluşturuldu.");
            return "redirect:/admin/kurslar";
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/course-form";
        }
    }

    @GetMapping("/{id}/duzenle")
    public String editCourseForm(@PathVariable Long id, Model model) {
        CourseResponse course = courseService.findById(id);
        model.addAttribute("course", course);
        CourseUpdateRequest dto = new CourseUpdateRequest();
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setPrice(course.getPrice());
        dto.setQuota(course.getQuota());
        dto.setStartDate(course.getStartDate());
        dto.setEndDate(course.getEndDate());
        dto.setHours(course.getHours());
        dto.setStatus(course.getStatus());
        dto.setLevel(course.getLevel());
        dto.setInstructorName(course.getInstructorName());
        dto.setInstructorId(course.getInstructorId());
        dto.setCourseType(course.getCourseType());
        dto.setLocation(course.getLocation());
        dto.setCourseSchedule(course.getCourseSchedule());
        dto.setScheduleDays(course.getScheduleDays());
        dto.setScheduleStartTime(course.getScheduleStartTime());
        dto.setScheduleEndTime(course.getScheduleEndTime());
        dto.setManualCurriculum(course.getManualCurriculum());
        if (course.getInstructors() != null && !course.getInstructors().isEmpty()) {
            dto.setInstructorIds(course.getInstructors().stream()
                    .map(InstructorResponse::getId)
                    .collect(java.util.stream.Collectors.toList()));
        }
        model.addAttribute("courseUpdateRequest", dto);
        return "admin/course-edit";
    }

    @PostMapping("/{id}")
    public String updateCourse(@PathVariable Long id,
                               @Valid @ModelAttribute("courseUpdateRequest") CourseUpdateRequest request,
                               BindingResult bindingResult,
                               @RequestParam(value = "image", required = false) MultipartFile image,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("course", courseService.findById(id));
            return "admin/course-edit";
        }
        try {
            courseService.update(id, request, image);
            redirectAttributes.addFlashAttribute("successMessage", "Kurs güncellendi.");
            return "redirect:/admin/kurslar";
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("course", courseService.findById(id));
            return "admin/course-edit";
        }
    }

    @PostMapping("/{id}/durum")
    public ActionResult changeCourseStatus(@PathVariable Long id,
                                           @RequestParam CourseStatus status) {
        try {
            courseService.changeStatus(id, status);
            return ActionResult.success("Kurs durumu güncellendi.", "/admin/kurslar");
        } catch (Exception e) {
            log.error("Kurs durumu değiştirilirken hata (id={}): {}", id, e.getMessage(), e);
            return ActionResult.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/sil")
    public ActionResult deleteCourse(@PathVariable Long id) {
        try {
            courseService.delete(id);
            return ActionResult.success("Kurs iptal edildi.", "/admin/kurslar");
        } catch (Exception e) {
            log.error("Kurs silinirken hata: {}", e.getMessage(), e);
            return ActionResult.error(e.getMessage());
        }
    }

    @PostMapping(value = "/{id}/onecikar", produces = "application/json")
    @ResponseBody
    public ActionResult toggleFeatured(@PathVariable Long id) {
        try {
            boolean newState = courseService.toggleFeatured(id);
            return ActionResult.success(newState ? "Kurs ana sayfada gösterilecek." : "Kurs ana sayfadan kaldırıldı.");
        } catch (Exception e) {
            log.error("Kurs öne çıkarma hatası: {}", e.getMessage(), e);
            return ActionResult.error(e.getMessage());
        }
    }
}
