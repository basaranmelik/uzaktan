package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.dto.request.CourseCreateRequest;
import com.guzem.uzaktan.dto.request.CourseUpdateRequest;
import com.guzem.uzaktan.dto.response.CourseResponse;
import com.guzem.uzaktan.model.CourseCategory;
import com.guzem.uzaktan.model.CourseLevel;
import com.guzem.uzaktan.model.CourseStatus;
import com.guzem.uzaktan.model.CourseType;
import com.guzem.uzaktan.model.Role;
import com.guzem.uzaktan.service.CourseService;
import com.guzem.uzaktan.service.InstructorService;
import com.guzem.uzaktan.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@PreAuthorize("hasRole('ADMIN')")
public class AdminCourseController {

    private final CourseService courseService;
    private final UserService userService;
    private final InstructorService instructorService;

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
        model.addAttribute("categories", CourseCategory.values());
        model.addAttribute("levels", CourseLevel.values());
        model.addAttribute("courseTypes", CourseType.values());
        model.addAttribute("teachers", userService.findUsersByRole(Role.TEACHER));
        model.addAttribute("instructors", instructorService.findAll());
        return "admin/course-form";
    }

    @PostMapping
    public String createCourse(@Valid @ModelAttribute("courseCreateRequest") CourseCreateRequest request,
                               BindingResult bindingResult,
                               @RequestParam(value = "image", required = false) MultipartFile image,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", CourseCategory.values());
            model.addAttribute("levels", CourseLevel.values());
            model.addAttribute("courseTypes", CourseType.values());
            model.addAttribute("teachers", userService.findUsersByRole(Role.TEACHER));
            model.addAttribute("instructors", instructorService.findAll());
            return "admin/course-form";
        }
        courseService.create(request, image);
        redirectAttributes.addFlashAttribute("successMessage", "Kurs başarıyla oluşturuldu.");
        return "redirect:/admin/kurslar";
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
        dto.setModule(course.getModule());
        dto.setCategory(course.getCategory());
        dto.setStatus(course.getStatus());
        dto.setLevel(course.getLevel());
        dto.setInstructorName(course.getInstructorName());
        dto.setInstructorId(course.getInstructorId());
        dto.setCourseType(course.getCourseType());
        dto.setLocation(course.getLocation());
        dto.setCourseSchedule(course.getCourseSchedule());
        dto.setCertificateDeadline(course.getCertificateDeadline());
        model.addAttribute("courseUpdateRequest", dto);
        model.addAttribute("categories", CourseCategory.values());
        model.addAttribute("levels", CourseLevel.values());
        model.addAttribute("statuses", CourseStatus.values());
        model.addAttribute("courseTypes", CourseType.values());
        model.addAttribute("teachers", userService.findUsersByRole(Role.TEACHER));
        model.addAttribute("instructors", instructorService.findAll());
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
            model.addAttribute("categories", CourseCategory.values());
            model.addAttribute("levels", CourseLevel.values());
            model.addAttribute("statuses", CourseStatus.values());
            model.addAttribute("courseTypes", CourseType.values());
            model.addAttribute("teachers", userService.findUsersByRole(Role.TEACHER));
            model.addAttribute("instructors", instructorService.findAll());
            return "admin/course-edit";
        }
        courseService.update(id, request, image);
        redirectAttributes.addFlashAttribute("successMessage", "Kurs güncellendi.");
        return "redirect:/admin/kurslar";
    }

    @PostMapping("/{id}/durum")
    public String changeCourseStatus(@PathVariable("id") Long id,
                                     @RequestParam("status") CourseStatus status,
                                     RedirectAttributes redirectAttributes) {
        courseService.changeStatus(id, status);
        redirectAttributes.addFlashAttribute("successMessage", "Kurs durumu güncellendi.");
        return "redirect:/admin/kurslar";
    }

    @PostMapping("/{id}/sil")
    public String deleteCourse(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        courseService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Kurs iptal edildi.");
        return "redirect:/admin/kurslar";
    }
}
