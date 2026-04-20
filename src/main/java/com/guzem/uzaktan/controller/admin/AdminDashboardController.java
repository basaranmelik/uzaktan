package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.model.Role;
import com.guzem.uzaktan.service.AssignmentService;
import com.guzem.uzaktan.service.CertificateService;
import com.guzem.uzaktan.service.CourseReviewService;
import com.guzem.uzaktan.service.CourseService;
import com.guzem.uzaktan.service.InstructorService;
import com.guzem.uzaktan.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final CourseService courseService;
    private final UserService userService;
    private final CertificateService certificateService;
    private final AssignmentService assignmentService;
    private final InstructorService instructorService;
    private final com.guzem.uzaktan.service.EnrollmentService enrollmentService;
    private final CourseReviewService courseReviewService;

    @GetMapping
    public String dashboard(Model model) {
        var courseCounts = courseService.getStatusCounts();
        long totalCourses = courseCounts.values().stream().mapToLong(v -> v).sum();
        model.addAttribute("courseCounts", courseCounts);
        model.addAttribute("typeCounts", courseService.getTypeCounts());
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("totalUsers", userService.findAllUsers().size());
        model.addAttribute("totalCertificates", certificateService.findAll().size());
        model.addAttribute("totalAssignments", assignmentService.countAllAssignments());
        model.addAttribute("pendingSubmissions", assignmentService.countPendingSubmissions());
        model.addAttribute("totalInstructors", userService.findUsersByRole(Role.TEACHER).size());
        model.addAttribute("totalEnrollments", enrollmentService.countTotal());
        model.addAttribute("pendingEnrollments", enrollmentService.countByStatus(com.guzem.uzaktan.model.EnrollmentStatus.PENDING_PAYMENT));
        model.addAttribute("totalReviews", courseReviewService.countAllReviews());
        model.addAttribute("pendingReviews", courseReviewService.countPendingReviews());
        return "admin/dashboard";
    }
}
