package com.guzem.uzaktan.controller.user;

import com.guzem.uzaktan.dto.response.AssignmentResponse;
import com.guzem.uzaktan.dto.response.CertificateResponse;
import com.guzem.uzaktan.dto.response.CourseDocumentResponse;
import com.guzem.uzaktan.dto.response.EnrollmentResponse;
import com.guzem.uzaktan.dto.response.CourseSummaryResponse;
import com.guzem.uzaktan.dto.response.SubmissionResponse;
import com.guzem.uzaktan.dto.response.UserResponse;
import com.guzem.uzaktan.service.course.CertificateService;
import com.guzem.uzaktan.service.course.CourseDocumentService;

import com.guzem.uzaktan.dto.request.ContactRequest;
import com.guzem.uzaktan.service.admin.AssignmentService;
import com.guzem.uzaktan.service.course.CourseService;
import com.guzem.uzaktan.service.course.EnrollmentService;
import com.guzem.uzaktan.service.common.GeneralEmailService;
import com.guzem.uzaktan.service.user.UserService;
import com.guzem.uzaktan.service.instructor.ZoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;

import com.guzem.uzaktan.model.course.EnrollmentStatus;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CourseService courseService;
    private final UserService userService;
    private final EnrollmentService enrollmentService;
    private final AssignmentService assignmentService;
    private final ZoomService zoomService;
    private final GeneralEmailService emailService;
    private final CertificateService certificateService;
    private final CourseDocumentService courseDocumentService;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        List<CourseSummaryResponse> featured = courseService.findFeaturedCourses();
        model.addAttribute("featuredCourses", featured.stream().limit(6).toList());
        return "home";
    }

    @GetMapping("/panom")
    @PreAuthorize("hasRole('USER')")
    public String dashboard(@AuthenticationPrincipal UserDetails principal,
                            @ModelAttribute("currentUserId") Long currentUserId,
                            Model model) {
        UserResponse user = userService.findByEmail(principal.getUsername());

        List<EnrollmentResponse> enrollments = enrollmentService.findByUser(currentUserId);
        List<AssignmentResponse> pendingAssignments = assignmentService.findPendingAssignmentsForStudent(currentUserId);
        List<AssignmentResponse> overdueAssignments = assignmentService.findOverdueAssignmentsForStudent(currentUserId);
        List<SubmissionResponse> submissions = assignmentService.findAllSubmissionsForStudent(currentUserId);

        model.addAttribute("user", user);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("activeCount", enrollmentService.countByUserAndStatus(currentUserId, EnrollmentStatus.ACTIVE));
        model.addAttribute("completedCount", enrollmentService.countByUserAndStatus(currentUserId, EnrollmentStatus.COMPLETED));
        model.addAttribute("pendingAssignments", pendingAssignments);
        model.addAttribute("pendingAssignmentCount", pendingAssignments.size());
        model.addAttribute("overdueAssignments", overdueAssignments);
        model.addAttribute("submissions", submissions);
        model.addAttribute("upcomingMeetings", zoomService.getUpcomingForStudent(currentUserId));
        model.addAttribute("allMeetings", zoomService.getAllForStudent(currentUserId));

        // Sınav geçilerek kazanılan sertifikalara sahip kurs ID'leri
        Set<Long> certifiedCourseIds = certificateService.findByUser(currentUserId).stream()
                .map(CertificateResponse::getCourseId)
                .collect(Collectors.toSet());
        model.addAttribute("certifiedCourseIds", certifiedCourseIds);

        List<CourseDocumentResponse> documents = new java.util.ArrayList<>();
        for (EnrollmentResponse enrollment : enrollments) {
            try {
                documents.addAll(courseDocumentService.findByCourse(enrollment.getCourseId()));
            } catch (Exception e) {
                log.warn("Doküman listesi alınamadı (kursId={}): {}", enrollment.getCourseId(), e.getMessage());
            }
        }
        model.addAttribute("courseDocuments", documents);

        return "dashboard";
    }

    @GetMapping("/hakkimizda")
    public String about() {
        return "hakkimizda";
    }

    @GetMapping("/iletisim")
    public String contact() {
        return "iletisim";
    }

    @PostMapping("/iletisim")
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Object>> submitContactForm(@Valid @RequestBody ContactRequest request) {
        try {
            emailService.sendContactEmail(request);
            return ResponseEntity.ok(java.util.Map.of("success", true, "message", "Mesajınız başarıyla iletildi."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("success", false, "message", "Mesaj gönderilirken bir hata oluştu."));
        }
    }

    @GetMapping("/kvkk")
    public String kvkk() {
        return "kvkk";
    }

    @GetMapping("/sss")
    public String sss() {
        return "sss";
    }

    @GetMapping("/kullanim")
    public String kullanim() {
        return "kullanim";
    }

    @GetMapping("/gizlilik")
    public String gizlilik() {
        return "gizlilik";
    }

    @GetMapping("/hata/404")
    public String notFound() {
        return "error/404";
    }
}
