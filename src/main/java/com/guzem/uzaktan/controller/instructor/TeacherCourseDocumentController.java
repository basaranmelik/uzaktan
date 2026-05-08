package com.guzem.uzaktan.controller.instructor;

import com.guzem.uzaktan.service.course.CourseDocumentService;
import com.guzem.uzaktan.service.course.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/egitmen/kurslarim/{courseId}/dokumanlar")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
public class TeacherCourseDocumentController {

    private final CourseDocumentService documentService;
    private final CourseService courseService;

    @GetMapping
    public String listDocuments(@PathVariable Long courseId,
                                @ModelAttribute("currentUserId") Long currentUserId,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            var course = courseService.findById(courseId);
            model.addAttribute("course", course);
            model.addAttribute("documents", documentService.findByCourse(courseId, currentUserId));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/egitmen/kurslarim";
        }
        return "egitmen/dokumanlar";
    }

    @PostMapping
    public String uploadDocument(@PathVariable Long courseId,
                                 @RequestParam("title") String title,
                                 @RequestParam("file") MultipartFile file,
                                 @ModelAttribute("currentUserId") Long currentUserId,
                                 RedirectAttributes redirectAttributes) {
        try {
            documentService.upload(courseId, title, file, currentUserId);
            redirectAttributes.addFlashAttribute("successMessage", "Doküman başarıyla yüklendi.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/egitmen/kurslarim/" + courseId + "/dokumanlar";
    }

    @PostMapping("/{documentId}/sil")
    public String deleteDocument(@PathVariable Long courseId,
                                 @PathVariable Long documentId,
                                 @ModelAttribute("currentUserId") Long currentUserId,
                                 RedirectAttributes redirectAttributes) {
        try {
            documentService.delete(documentId, currentUserId);
            redirectAttributes.addFlashAttribute("successMessage", "Doküman silindi.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/egitmen/kurslarim/" + courseId + "/dokumanlar";
    }
}
