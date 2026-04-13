package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.service.CourseService;
import com.guzem.uzaktan.service.CourseVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminVideoController {

    private final CourseService courseService;
    private final CourseVideoService courseVideoService;

    @GetMapping("/admin/kurslar/{courseId}/videolar")
    public String courseVideos(@PathVariable Long courseId, Model model) {
        model.addAttribute("course", courseService.findById(courseId));
        model.addAttribute("videos", courseVideoService.findByCourse(courseId));
        return "admin/course-videos";
    }

    @PostMapping("/admin/kurslar/{courseId}/videolar")
    public String uploadVideos(@PathVariable Long courseId,
                               @RequestParam("files") MultipartFile[] files,
                               @RequestParam(value = "titles", required = false) String[] titles,
                               @RequestParam(value = "orderIndices", required = false) Integer[] orderIndices,
                               RedirectAttributes redirectAttributes) {
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
    public String deleteVideo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        var video = courseVideoService.findById(id);
        Long courseId = video.getCourseId();
        courseVideoService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Video silindi.");
        return "redirect:/admin/kurslar/" + courseId + "/videolar";
    }

    @PostMapping(value = "/admin/kurslar/{courseId}/videolar/sira", consumes = "application/json")
    @ResponseBody
    public ResponseEntity<Void> reorderVideos(@PathVariable Long courseId,
                                              @RequestBody List<Long> orderedIds) {
        courseVideoService.updateOrder(courseId, orderedIds);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/videolar/{id}/duzenle")
    public String updateVideo(@PathVariable Long id,
                              @RequestParam String title,
                              @RequestParam(required = false) String description,
                              RedirectAttributes redirectAttributes) {
        Long courseId = courseVideoService.findById(id).getCourseId();
        try {
            courseVideoService.update(id, title, description);
            redirectAttributes.addFlashAttribute("successMessage", "Video güncellendi.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Güncelleme hatası: " + e.getMessage());
        }
        return "redirect:/admin/kurslar/" + courseId + "/videolar";
    }
}
