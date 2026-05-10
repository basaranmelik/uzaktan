package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.dto.response.ActionResult;
import com.guzem.uzaktan.model.common.Role;
import com.guzem.uzaktan.service.course.CourseService;
import com.guzem.uzaktan.service.course.CourseVideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','TEACHER','FIRM')")
public class AdminVideoController {

    private final CourseService courseService;
    private final CourseVideoService courseVideoService;

    @GetMapping("/kurslar/{courseId}/videolar")
    public String courseVideos(@PathVariable Long courseId,
                               @ModelAttribute("currentUserId") Long currentUserId,
                               @AuthenticationPrincipal UserDetails principal,
                               Model model) {
        ensureCanManageCourse(courseId, currentUserId, principal);
        model.addAttribute("course", courseService.findById(courseId));
        model.addAttribute("videos", courseVideoService.findByCourse(courseId));
        return "admin/course-videos";
    }

    @PostMapping("/kurslar/{courseId}/videolar")
    public ActionResult uploadVideos(@PathVariable Long courseId,
                                     @ModelAttribute("currentUserId") Long currentUserId,
                                     @AuthenticationPrincipal UserDetails principal,
                                     @RequestParam("files") MultipartFile[] files,
                                     @RequestParam(value = "titles", required = false) String[] titles,
                                     @RequestParam(value = "orderIndices", required = false) Integer[] orderIndices) {
        ensureCanManageCourse(courseId, currentUserId, principal);
        try {
            long nonEmpty = java.util.Arrays.stream(files).filter(f -> !f.isEmpty()).count();
            if (nonEmpty == 0) {
                return ActionResult.error("Lütfen en az bir video dosyası seçin.");
            }
            courseVideoService.uploadMultiple(courseId, files, titles, orderIndices);
            return ActionResult.success(nonEmpty + " video başarıyla yüklendi.",
                    "/admin/kurslar/" + courseId + "/videolar");
        } catch (IllegalArgumentException e) {
            return ActionResult.error(e.getMessage());
        } catch (Exception e) {
            log.error("Video yükleme hatası: {}", e.getMessage(), e);
            return ActionResult.error("Video yüklenirken beklenmeyen hata: " + e.getMessage());
        }
    }

    @PostMapping("/videolar/{id}/sil")
    public ActionResult deleteVideo(@PathVariable Long id,
                                    @ModelAttribute("currentUserId") Long currentUserId,
                                    @AuthenticationPrincipal UserDetails principal) {
        try {
            var video = courseVideoService.findById(id);
            Long courseId = video.getCourseId();
            ensureCanManageCourse(courseId, currentUserId, principal);
            courseVideoService.delete(id);
            return ActionResult.success("Video silindi.", "/admin/kurslar/" + courseId + "/videolar");
        } catch (Exception e) {
            log.error("Video silinirken hata: {}", e.getMessage(), e);
            return ActionResult.error(e.getMessage());
        }
    }

    @PostMapping(value = "/kurslar/{courseId}/videolar/sira", consumes = "application/json")
    @ResponseBody
    public ResponseEntity<Void> reorderVideos(@PathVariable Long courseId,
                                              @ModelAttribute("currentUserId") Long currentUserId,
                                              @AuthenticationPrincipal UserDetails principal,
                                              @RequestBody List<Long> orderedIds) {
        ensureCanManageCourse(courseId, currentUserId, principal);
        courseVideoService.updateOrder(courseId, orderedIds);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/videolar/{id}/duzenle")
    public ActionResult updateVideo(@PathVariable Long id,
                                    @ModelAttribute("currentUserId") Long currentUserId,
                                    @AuthenticationPrincipal UserDetails principal,
                                    @RequestParam String title,
                                    @RequestParam(required = false) String description) {
        Long courseId = courseVideoService.findById(id).getCourseId();
        ensureCanManageCourse(courseId, currentUserId, principal);
        try {
            courseVideoService.update(id, title, description);
            return ActionResult.success("Video güncellendi.", "/admin/kurslar/" + courseId + "/videolar");
        } catch (Exception e) {
            log.error("Video güncelleme hatası: {}", e.getMessage(), e);
            return ActionResult.error("Güncelleme hatası: " + e.getMessage());
        }
    }

    private void ensureCanManageCourse(Long courseId, Long currentUserId, UserDetails principal) {
        var course = courseService.findById(courseId);
        boolean isAdminOrFirm = principal.getAuthorities().stream()
                .anyMatch(a -> Role.ADMIN.getAuthority().equals(a.getAuthority()) || Role.FIRM.getAuthority().equals(a.getAuthority()));
        if (isAdminOrFirm) {
            return;
        }
        if (course.getInstructorId() == null || !course.getInstructorId().equals(currentUserId)) {
            throw new AccessDeniedException("Bu kursun videolarini yonetme yetkiniz yok.");
        }
    }
}
