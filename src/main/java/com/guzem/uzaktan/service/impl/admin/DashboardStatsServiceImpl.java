package com.guzem.uzaktan.service.impl.admin;

import com.guzem.uzaktan.model.common.Role;
import com.guzem.uzaktan.model.course.EnrollmentStatus;
import com.guzem.uzaktan.service.admin.AssignmentService;
import com.guzem.uzaktan.service.admin.DashboardStatsService;
import com.guzem.uzaktan.service.course.CertificateService;
import com.guzem.uzaktan.service.course.CourseReviewService;
import com.guzem.uzaktan.service.course.CourseService;
import com.guzem.uzaktan.service.course.EnrollmentService;
import com.guzem.uzaktan.service.instructor.ZoomService;
import com.guzem.uzaktan.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardStatsServiceImpl implements DashboardStatsService {

    private final CourseService courseService;
    private final UserService userService;
    private final CertificateService certificateService;
    private final AssignmentService assignmentService;
    private final EnrollmentService enrollmentService;
    private final CourseReviewService courseReviewService;
    private final ZoomService zoomService;

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        var courseCounts = courseService.getStatusCounts();
        long totalCourses = courseCounts.values().stream().mapToLong(v -> v).sum();

        stats.put("courseCounts", courseCounts);
        stats.put("typeCounts", courseService.getTypeCounts());
        stats.put("totalCourses", totalCourses);
        stats.put("totalUsers", userService.findAllUsers().size());
        stats.put("totalCertificates", certificateService.findAll().size());
        stats.put("totalAssignments", assignmentService.countAllAssignments());
        stats.put("pendingSubmissions", assignmentService.countPendingSubmissions());
        stats.put("totalInstructors", userService.findUsersByRole(Role.TEACHER).size());
        stats.put("totalEnrollments", enrollmentService.countTotal());
        stats.put("pendingEnrollments", enrollmentService.countByStatus(EnrollmentStatus.PENDING_PAYMENT));
        stats.put("totalReviews", courseReviewService.countAllReviews());
        stats.put("pendingReviews", courseReviewService.countPendingReviews());
        stats.put("totalMeetings", zoomService.countAllMeetings());
        stats.put("startedMeetings", zoomService.countStartedMeetings());
        stats.put("missedMeetings", zoomService.countMissedMeetings());
        stats.put("recordedMeetings", zoomService.countRecordedMeetings());

        return stats;
    }
}
