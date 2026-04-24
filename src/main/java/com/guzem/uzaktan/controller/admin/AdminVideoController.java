package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.service.course.CourseService;
import com.guzem.uzaktan.service.course.CourseVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','TEACHER','FIRM')")
public class AdminVideoController {

    private final CourseService courseService;
    private final CourseVideoService courseVideoService;

    @GetMapping("/admin/kurslar/{courseId}/videolar")
    public String courseVideos(@PathVariable Long courseId,
                               @ModelAttribute("currentUserId") Long currentUserId,
                               @AuthenticationPrincipal UserDetails principal,
                               Model model) {
        ensureCanManageCourse(courseId, currentUserId, principal);
        model.addAttribute("course", courseService.findById(courseId));
        model.addAttribute("videos", courseVideoService.findByCourse(courseId));
        return "admin/course-videos";
    }

    @PostMapping("/admin/kurslar/{courseId}/videolar")
    public String uploadVideos(@PathVariable Long courseId,
                               @ModelAttribute("currentUserId") Long currentUserId,
                               @AuthenticationPrincipal UserDetails principal,
                               @RequestParam("files") MultipartFile[] files,
                               @RequestParam(value = "titles", required = false) String[] titles,
                               @RequestParam(value = "orderIndices", required = false) Integer[] orderIndices,
                               RedirectAttributes redirectAttributes) {
        ensureCanManageCourse(courseId, currentUserId, principal);
        try {
            long nonEmpty = java.util.Arrays.stream(files).filter(f -> !f.isEmpty()).count();
            if (nonEmpty == 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Lütfen en az bir video dosyası seçin.");
                return "redirect:/admin/kurslar/" + courseId + "/videolar";
            }
            courseVideoService.uploadMultiple(courseId, files, titles, orderIndices);
            redirectAttributes.addFlashAttribute("successMessage",
                    nonEmpty + " video başarıyla yüklendi.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Video yüklenirken hata oluştu: " + e.getMessage());
        }
        return "redirect:/admin/kurslar/" + courseId + "/videolar";
    }

    @PostMapping("/admin/videolar/{id}/sil")
    public String deleteVideo(@PathVariable Long id,
                              @ModelAttribute("currentUserId") Long currentUserId,
                              @AuthenticationPrincipal UserDetails principal,
                              RedirectAttributes redirectAttributes) {
        var video = courseVideoService.findById(id);
        Long courseId = video.getCourseId();
        ensureCanManageCourse(courseId, currentUserId, principal);
        courseVideoService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Video silindi.");
        return "redirect:/admin/kurslar/" + courseId + "/videolar";
    }

    @PostMapping(value = "/admin/kurslar/{courseId}/videolar/sira", consumes = "application/json")
    @ResponseBody
    public ResponseEntity<Void> reorderVideos(@PathVariable Long courseId,
                                              @ModelAttribute("currentUserId") Long currentUserId,
                                              @AuthenticationPrincipal UserDetails principal,
                                              @RequestBody List<Long> orderedIds) {
        ensureCanManageCourse(courseId, currentUserId, principal);
        courseVideoService.updateOrder(courseId, orderedIds);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/videolar/{id}/duzenle")
    public String updateVideo(@PathVariable Long id,
                              @ModelAttribute("currentUserId") Long currentUserId,
                              @AuthenticationPrincipal UserDetails principal,
                              @RequestParam String title,
                              @RequestParam(required = false) String description,
                              RedirectAttributes redirectAttributes) {
        Long courseId = courseVideoService.findById(id).getCourseId();
        ensureCanManageCourse(courseId, currentUserId, principal);
        try {
            courseVideoService.update(id, title, description);
            redirectAttributes.addFlashAttribute("successMessage", "Video güncellendi.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Güncelleme hatası: " + e.getMessage());
        }
        return "redirect:/admin/kurslar/" + courseId + "/videolar";
    }

    private void ensureCanManageCourse(Long courseId, Long currentUserId, UserDetails principal) {
        var course = courseService.findById(courseId);
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (isAdmin) {
            return;
        }
        if (course.getInstructorId() == null || !course.getInstructorId().equals(currentUserId)) {
            throw new AccessDeniedException("Bu kursun videolarini yonetme yetkiniz yok.");
        }
    }
}
