package com.guzem.uzaktan.controller.instructor;

import com.guzem.uzaktan.dto.request.AssignmentCreateRequest;
import com.guzem.uzaktan.dto.request.AssignmentUpdateRequest;
import com.guzem.uzaktan.dto.request.GradeSubmissionRequest;
import com.guzem.uzaktan.dto.response.AssignmentResponse;
import com.guzem.uzaktan.dto.response.CourseResponse;
import com.guzem.uzaktan.dto.response.SubmissionResponse;
import com.guzem.uzaktan.dto.response.UserResponse;
import com.guzem.uzaktan.model.common.Role;
import com.guzem.uzaktan.model.admin.SubmissionStatus;
import com.guzem.uzaktan.service.admin.AssignmentService;
import com.guzem.uzaktan.service.course.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Eğitmen ödev yönetimi — ödev CRUD işlemleri ve teslim değerlendirmesi.
 * URL prefix: /egitmen
 */
@Controller
@RequestMapping("/egitmen")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
public class TeacherAssignmentController {

    private final CourseService courseService;
    private final AssignmentService assignmentService;

    // ---- Ödev Yönetimi ----

    @GetMapping("/kurslarim/{courseId}/odevler")
    public String courseAssignments(@PathVariable Long courseId,
                                    @ModelAttribute("currentUserId") Long currentUserId,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        CourseResponse course = courseService.findById(courseId);
        if (!isCourseOwnerOrAdmin(course, currentUserId, null)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bu kursa erişim yetkiniz bulunmamaktadır.");
            return "redirect:/egitmen/panel";
        }
        model.addAttribute("course", course);
        model.addAttribute("assignments", assignmentService.findByCourse(courseId));
        return "egitmen/kurs-odevleri";
    }

    @GetMapping("/kurslarim/{courseId}/odevler/yeni")
    public String newAssignmentForm(@PathVariable Long courseId,
                                    @ModelAttribute("currentUserId") Long currentUserId,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        CourseResponse course = courseService.findById(courseId);
        if (!isCourseOwnerOrAdmin(course, currentUserId, null)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bu kursa erişim yetkiniz bulunmamaktadır.");
            return "redirect:/egitmen/panel";
        }
        model.addAttribute("course", course);
        model.addAttribute("assignmentCreateRequest", new AssignmentCreateRequest());
        return "egitmen/odev-form";
    }

    @PostMapping("/kurslarim/{courseId}/odevler")
    public String createAssignment(@PathVariable Long courseId,
                                   @Valid @ModelAttribute("assignmentCreateRequest") AssignmentCreateRequest request,
                                   BindingResult bindingResult,
                                   @ModelAttribute("currentUserId") Long currentUserId,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("course", courseService.findById(courseId));
            return "egitmen/odev-form";
        }
        assignmentService.createAssignment(courseId, request, currentUserId);
        redirectAttributes.addFlashAttribute("successMessage", "Ödev başarıyla oluşturuldu.");
        return "redirect:/egitmen/kurslarim/" + courseId + "/odevler";
    }

    @GetMapping("/odevler/{id}/duzenle")
    public String editAssignmentForm(@PathVariable Long id,
                                     @ModelAttribute("currentUserId") Long currentUserId,
                                     Model model) {
        var assignment = assignmentService.findById(id, currentUserId);
        model.addAttribute("assignment", assignment);
        AssignmentUpdateRequest dto = new AssignmentUpdateRequest();
        dto.setTitle(assignment.getTitle());
        dto.setDescription(assignment.getDescription());
        dto.setDueDate(assignment.getDueDate());
        dto.setMaxScore(assignment.getMaxScore());
        model.addAttribute("assignmentUpdateRequest", dto);
        return "egitmen/odev-duzenle";
    }

    @PostMapping("/odevler/{id}")
    public String updateAssignment(@PathVariable Long id,
                                   @Valid @ModelAttribute("assignmentUpdateRequest") AssignmentUpdateRequest request,
                                   BindingResult bindingResult,
                                   @ModelAttribute("currentUserId") Long currentUserId,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("assignment", assignmentService.findById(id, currentUserId));
            return "egitmen/odev-duzenle";
        }
        AssignmentResponse updated = assignmentService.updateAssignment(id, request, currentUserId);
        redirectAttributes.addFlashAttribute("successMessage", "Ödev güncellendi.");
        return "redirect:/egitmen/kurslarim/" + updated.getCourseId() + "/odevler";
    }

    @PostMapping("/odevler/{id}/sil")
    public String deleteAssignment(@PathVariable Long id,
                                   @ModelAttribute("currentUserId") Long currentUserId,
                                   RedirectAttributes redirectAttributes) {
        AssignmentResponse assignment = assignmentService.findById(id, currentUserId);
        Long courseId = assignment.getCourseId();
        assignmentService.deleteAssignment(id, currentUserId);
        redirectAttributes.addFlashAttribute("successMessage", "Ödev silindi.");
        return "redirect:/egitmen/kurslarim/" + courseId + "/odevler";
    }

    // ---- Teslim Yönetimi ----

    @GetMapping("/odevler/{id}/teslimler")
    public String submissions(@PathVariable Long id,
                              @ModelAttribute("currentUserId") Long currentUserId,
                              Model model) {
        AssignmentResponse assignment = assignmentService.findById(id, currentUserId);
        List<SubmissionResponse> submissions = assignmentService.findSubmissionsByAssignment(id, currentUserId);

        long gradedCount = submissions.stream()
                .filter(s -> s.getStatus() == SubmissionStatus.GRADED)
                .count();
        double averageScore = submissions.stream()
                .filter(s -> s.getScore() != null)
                .mapToInt(SubmissionResponse::getScore)
                .average()
                .orElse(0.0);
        List<UserResponse> notSubmitted =
                assignmentService.findNotSubmittedStudents(assignment.getCourseId(), id);
        long enrolledCount = submissions.size() + notSubmitted.size();

        model.addAttribute("assignment", assignment);
        model.addAttribute("submissions", submissions);
        model.addAttribute("notSubmittedStudents", notSubmitted);
        model.addAttribute("enrolledCount", enrolledCount);
        model.addAttribute("gradedCount", gradedCount);
        model.addAttribute("averageScore", Math.round(averageScore * 10.0) / 10.0);
        return "egitmen/teslimler";
    }

    @GetMapping("/odevler/{id}/teslimler-indir")
    public ResponseEntity<byte[]> downloadSubmissionsZip(@PathVariable Long id,
                                                         @ModelAttribute("currentUserId") Long currentUserId) throws java.io.IOException {
        byte[] zip = assignmentService.downloadSubmissionsZip(id, currentUserId);
        var assignment = assignmentService.findById(id, currentUserId);
        String filename = com.guzem.uzaktan.service.impl.common.LocalFileStorageService.sanitizeFileName(assignment.getTitle()) + "_teslimler.zip";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/zip"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(zip);
    }

    @GetMapping("/teslimler/{submissionId}/notlandir")
    public String gradeForm(@PathVariable Long submissionId,
                            @ModelAttribute("currentUserId") Long currentUserId,
                            Model model) {
        var submission = assignmentService.findSubmissionById(submissionId, currentUserId);
        model.addAttribute("submission", submission);
        GradeSubmissionRequest dto = new GradeSubmissionRequest();
        if (submission.getScore() != null) dto.setScore(submission.getScore());
        if (submission.getFeedback() != null) dto.setFeedback(submission.getFeedback());
        model.addAttribute("gradeSubmissionRequest", dto);
        return "egitmen/notlandir";
    }

    @PostMapping("/teslimler/{submissionId}/notlandir")
    public String gradeSubmission(@PathVariable Long submissionId,
                                  @Valid @ModelAttribute("gradeSubmissionRequest") GradeSubmissionRequest request,
                                  BindingResult bindingResult,
                                  @ModelAttribute("currentUserId") Long currentUserId,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("submission", assignmentService.findSubmissionById(submissionId, currentUserId));
            return "egitmen/notlandir";
        }
        var graded = assignmentService.gradeSubmission(submissionId, request, currentUserId);
        redirectAttributes.addFlashAttribute("successMessage", "Not kaydedildi.");
        return "redirect:/egitmen/odevler/" + graded.getAssignmentId() + "/teslimler";
    }

    // ---- Yardımcı ----

    private boolean isCourseOwnerOrAdmin(CourseResponse course, Long userId, UserDetails principal) {
        boolean isAdmin = (principal != null) && principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Role.ADMIN.getAuthority()));
        return isAdmin || (course.getInstructorId() != null && course.getInstructorId().equals(userId));
    }
}
