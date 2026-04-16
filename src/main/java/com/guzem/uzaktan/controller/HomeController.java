package com.guzem.uzaktan.controller;

import com.guzem.uzaktan.dto.response.AssignmentResponse;
import com.guzem.uzaktan.dto.response.EnrollmentResponse;
import com.guzem.uzaktan.dto.response.SubmissionResponse;
import com.guzem.uzaktan.dto.response.UserResponse;

import com.guzem.uzaktan.service.AssignmentService;
import com.guzem.uzaktan.service.CourseService;
import com.guzem.uzaktan.service.EnrollmentService;
import com.guzem.uzaktan.service.UserService;
import com.guzem.uzaktan.service.ZoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.guzem.uzaktan.model.EnrollmentStatus;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CourseService courseService;
    private final UserService userService;
    private final EnrollmentService enrollmentService;
    private final AssignmentService assignmentService;
    private final ZoomService zoomService;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("featuredCourses", courseService.findPublishedCourses(0, 6).getContent());
        return "home";
    }

    @GetMapping("/panom")
    public String dashboard(@AuthenticationPrincipal UserDetails principal, Model model) {
        UserResponse user = userService.findByEmail(principal.getUsername());
        Long userId = user.getId();

        List<EnrollmentResponse> enrollments = enrollmentService.findByUser(userId);
        List<AssignmentResponse> pendingAssignments = assignmentService.findPendingAssignmentsForStudent(userId);
        List<SubmissionResponse> submissions = assignmentService.findAllSubmissionsForStudent(userId);

        model.addAttribute("user", user);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("activeCount", enrollmentService.countByUserAndStatus(userId, EnrollmentStatus.ACTIVE));
        model.addAttribute("completedCount", enrollmentService.countByUserAndStatus(userId, EnrollmentStatus.COMPLETED));
        model.addAttribute("pendingAssignments", pendingAssignments);
        model.addAttribute("pendingAssignmentCount", pendingAssignments.size());
        model.addAttribute("submissions", submissions);
        model.addAttribute("upcomingMeetings", zoomService.getUpcomingForStudent(userId));
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
}
