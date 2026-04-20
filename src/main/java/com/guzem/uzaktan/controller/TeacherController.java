package com.guzem.uzaktan.controller;

import com.guzem.uzaktan.dto.request.AnnouncementRequest;
import com.guzem.uzaktan.dto.request.AssignmentCreateRequest;
import com.guzem.uzaktan.dto.request.AssignmentUpdateRequest;
import com.guzem.uzaktan.dto.request.GradeSubmissionRequest;
import com.guzem.uzaktan.dto.response.AssignmentResponse;
import com.guzem.uzaktan.dto.response.CourseResponse;
import com.guzem.uzaktan.dto.response.SubmissionResponse;
import com.guzem.uzaktan.model.Role;
import com.guzem.uzaktan.model.SubmissionStatus;
import com.guzem.uzaktan.model.NotificationType;
import com.guzem.uzaktan.model.User;
import com.guzem.uzaktan.repository.AssignmentRepository;
import com.guzem.uzaktan.repository.EnrollmentRepository;
import com.guzem.uzaktan.service.AssignmentService;
import com.guzem.uzaktan.service.CourseService;
import com.guzem.uzaktan.service.EmailService;
import com.guzem.uzaktan.service.NotificationService;
import com.guzem.uzaktan.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/egitmen")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
public class TeacherController {

    private final CourseService courseService;
    private final AssignmentService assignmentService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final EnrollmentRepository enrollmentRepository;
    private final AssignmentRepository assignmentRepository;

    // ---- Panel ----

    @GetMapping("/panel")
    public String panel(@AuthenticationPrincipal UserDetails principal, Model model) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        var courses = courseService.findByInstructor(userId);
        var courseIds = courses.stream().map(CourseResponse::getId).toList();
        model.addAttribute("courses", courses);
        model.addAttribute("courseCount", courses.size());
        model.addAttribute("totalStudents", courseService.countTotalStudentsForInstructor(userId));
        model.addAttribute("activeCourses", courseService.countActiveCoursesForInstructor(userId));
        model.addAttribute("totalAssignments", courseIds.isEmpty() ? 0 : assignmentRepository.countByCourseIdIn(courseIds));
        return "egitmen/panel";
    }

    @GetMapping("/kurslarim")
    public String myCourses(@AuthenticationPrincipal UserDetails principal, Model model) {
        model.addAttribute("courses", courseService.findByInstructor(userService.findUserIdByEmail(principal.getUsername())));
        return "egitmen/kurslarim";
    }

    // ---- Duyuru (Announcement) Gönderimi ----

    @GetMapping("/duyuru")
    public String announcementForm(@AuthenticationPrincipal UserDetails principal, Model model) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        var courses = courseService.findByInstructor(userId);
        model.addAttribute("courses", courses);
        model.addAttribute("announcementRequest", new AnnouncementRequest());
        return "egitmen/duyuru";
    }

    @PostMapping("/duyuru")
    public String sendAnnouncement(@Valid @ModelAttribute("announcementRequest") AnnouncementRequest request,
                                   BindingResult bindingResult,
                                   @AuthenticationPrincipal UserDetails principal,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        var courses = courseService.findByInstructor(userId);

        if (bindingResult.hasErrors()) {
            model.addAttribute("courses", courses);
            return "egitmen/duyuru";
        }

        List<Long> instructorCourseIds = courses.stream().map(CourseResponse::getId).toList();
        
        int totalSent = 0;
        Long selectedCourseId = request.getCourseId();

        if (selectedCourseId != null && instructorCourseIds.contains(selectedCourseId)) {
            var enrollments = enrollmentRepository.findActiveEnrollmentsForCourse(selectedCourseId);
            var courseInfo = courses.stream().filter(c -> c.getId().equals(selectedCourseId)).findFirst().orElse(null);
            String courseTitle = courseInfo != null ? courseInfo.getTitle() : "Kurs";
            
            for (var enrollment : enrollments) {
                User student = enrollment.getUser();
                
                // Sisteme bildirim kaydı at
                notificationService.create(student, NotificationType.COURSE_ANNOUNCEMENT, request.getSubject(), request.getMessage(), "/egitimler/izle/" + selectedCourseId);
                
                // Arka planda e-posta gönder
                emailService.sendCourseAnnouncement(student, courseTitle, request.getSubject(), request.getMessage());
                
                totalSent++;
            }
        } else {
             redirectAttributes.addFlashAttribute("errorMessage", "Geçersiz kurs seçimi.");
             return "redirect:/egitmen/panel";
        }

        redirectAttributes.addFlashAttribute("successMessage", totalSent + " öğrenciye duyuru başarıyla gönderildi.");
        return "redirect:/egitmen/panel";
    }

    // ---- Ödev Yönetimi ----

    @GetMapping("/kurslarim/{courseId}/odevler")
    public String courseAssignments(@PathVariable Long courseId,
                                    @AuthenticationPrincipal UserDetails principal,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        CourseResponse course = courseService.findById(courseId);
        if (!isCourseOwnerOrAdmin(course, userId, principal)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bu kursa erişim yetkiniz bulunmamaktadır.");
            return "redirect:/egitmen/panel";
        }
        model.addAttribute("course", course);
        model.addAttribute("assignments", assignmentService.findByCourse(courseId));
        return "egitmen/kurs-odevleri";
    }

    @GetMapping("/kurslarim/{courseId}/odevler/yeni")
    public String newAssignmentForm(@PathVariable Long courseId,
                                    @AuthenticationPrincipal UserDetails principal,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        CourseResponse course = courseService.findById(courseId);
        if (!isCourseOwnerOrAdmin(course, userId, principal)) {
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
                                   @AuthenticationPrincipal UserDetails principal,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("course", courseService.findById(courseId));
            return "egitmen/odev-form";
        }
        assignmentService.createAssignment(courseId, request, userService.findUserIdByEmail(principal.getUsername()));
        redirectAttributes.addFlashAttribute("successMessage", "Ödev başarıyla oluşturuldu.");
        return "redirect:/egitmen/kurslarim/" + courseId + "/odevler";
    }

    @GetMapping("/odevler/{id}/duzenle")
    public String editAssignmentForm(@PathVariable Long id, 
                                     @AuthenticationPrincipal UserDetails principal,
                                     Model model) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        var assignment = assignmentService.findById(id, userId);
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
                                   @AuthenticationPrincipal UserDetails principal,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            Long userId = userService.findUserIdByEmail(principal.getUsername());
            model.addAttribute("assignment", assignmentService.findById(id, userId));
            return "egitmen/odev-duzenle";
        }
        AssignmentResponse updated = assignmentService.updateAssignment(id, request, userService.findUserIdByEmail(principal.getUsername()));
        redirectAttributes.addFlashAttribute("successMessage", "Ödev güncellendi.");
        return "redirect:/egitmen/kurslarim/" + updated.getCourseId() + "/odevler";
    }

    @PostMapping("/odevler/{id}/sil")
    public String deleteAssignment(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails principal,
                                   RedirectAttributes redirectAttributes) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        AssignmentResponse assignment = assignmentService.findById(id, userId);
        Long courseId = assignment.getCourseId();
        assignmentService.deleteAssignment(id, userService.findUserIdByEmail(principal.getUsername()));
        redirectAttributes.addFlashAttribute("successMessage", "Ödev silindi.");
        return "redirect:/egitmen/kurslarim/" + courseId + "/odevler";
    }

    // ---- Teslim Yönetimi ----

    @GetMapping("/odevler/{id}/teslimler")
    public String submissions(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails principal,
                              Model model) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        AssignmentResponse assignment = assignmentService.findById(id, userId);
        java.util.List<SubmissionResponse> submissions = assignmentService.findSubmissionsByAssignment(id, userId);

        long gradedCount = submissions.stream()
                .filter(s -> s.getStatus() == SubmissionStatus.GRADED)
                .count();
        double averageScore = submissions.stream()
                .filter(s -> s.getScore() != null)
                .mapToInt(SubmissionResponse::getScore)
                .average()
                .orElse(0.0);
        java.util.List<com.guzem.uzaktan.dto.response.UserResponse> notSubmitted =
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
                                                         @AuthenticationPrincipal UserDetails principal) throws java.io.IOException {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        byte[] zip = assignmentService.downloadSubmissionsZip(id, userId);
        var assignment = assignmentService.findById(id, userId);
        String filename = com.guzem.uzaktan.service.impl.LocalFileStorageService.sanitizeFileName(assignment.getTitle()) + "_teslimler.zip";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/zip"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(zip);
    }

    @GetMapping("/teslimler/{submissionId}/notlandir")
    public String gradeForm(@PathVariable Long submissionId, 
                            @AuthenticationPrincipal UserDetails principal,
                            Model model) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        var submission = assignmentService.findSubmissionById(submissionId, userId);
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
                                  @AuthenticationPrincipal UserDetails principal,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            Long userId = userService.findUserIdByEmail(principal.getUsername());
            model.addAttribute("submission", assignmentService.findSubmissionById(submissionId, userId));
            return "egitmen/notlandir";
        }
        var graded = assignmentService.gradeSubmission(submissionId, request, userService.findUserIdByEmail(principal.getUsername()));
        redirectAttributes.addFlashAttribute("successMessage", "Not kaydedildi.");
        return "redirect:/egitmen/odevler/" + graded.getAssignmentId() + "/teslimler";
    }

    private boolean isCourseOwnerOrAdmin(CourseResponse course, Long userId, UserDetails principal) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Role.ADMIN.getAuthority()));
        return isAdmin || (course.getInstructorId() != null && course.getInstructorId().equals(userId));
    }
}
