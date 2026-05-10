package com.guzem.uzaktan.controller.instructor;

import com.guzem.uzaktan.dto.request.AnnouncementRequest;
import com.guzem.uzaktan.dto.response.CourseResponse;
import com.guzem.uzaktan.model.user.NotificationType;
import com.guzem.uzaktan.model.common.User;
import com.guzem.uzaktan.model.course.Enrollment;
import com.guzem.uzaktan.service.admin.AssignmentService;
import com.guzem.uzaktan.service.course.CourseService;
import com.guzem.uzaktan.service.course.EnrollmentService;
import com.guzem.uzaktan.service.common.GeneralEmailService;
import com.guzem.uzaktan.service.user.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Eğitmen paneli — genel görünüm, kurs listesi ve duyuru gönderimi.
 * URL prefix: /egitmen
 */
@Controller
@RequestMapping("/egitmen")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
public class TeacherPanelController {

    private final CourseService courseService;
    private final NotificationService notificationService;
    private final GeneralEmailService emailService;
    private final EnrollmentService enrollmentService;
    private final AssignmentService assignmentService;

    // ---- Panel ----

    @GetMapping("/panel")
    public String panel(@ModelAttribute("currentUserId") Long currentUserId, Model model) {
        var courses = courseService.findByInstructor(currentUserId);
        var courseIds = courses.stream().map(CourseResponse::getId).toList();
        model.addAttribute("courses", courses);
        model.addAttribute("courseCount", courses.size());
        model.addAttribute("totalStudents", courseService.countTotalStudentsForInstructor(currentUserId));
        model.addAttribute("activeCourses", courseService.countActiveCoursesForInstructor(currentUserId));
        model.addAttribute("totalAssignments", courseIds.isEmpty() ? 0 : assignmentService.countByCourseIdIn(courseIds));
        return "egitmen/panel";
    }

    @GetMapping("/kurslarim")
    public String myCourses(@ModelAttribute("currentUserId") Long currentUserId, Model model) {
        model.addAttribute("courses", courseService.findByInstructor(currentUserId));
        return "egitmen/kurslarim";
    }

    // ---- Duyuru (Announcement) Gönderimi ----

    @GetMapping("/duyuru")
    public String announcementForm(@ModelAttribute("currentUserId") Long currentUserId, Model model) {
        var courses = courseService.findByInstructor(currentUserId);
        model.addAttribute("courses", courses);
        model.addAttribute("announcementRequest", new AnnouncementRequest());
        return "egitmen/duyuru";
    }

    @PostMapping("/duyuru")
    public String sendAnnouncement(@Valid @ModelAttribute("announcementRequest") AnnouncementRequest request,
                                   BindingResult bindingResult,
                                   @ModelAttribute("currentUserId") Long currentUserId,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        var courses = courseService.findByInstructor(currentUserId);

        if (bindingResult.hasErrors()) {
            model.addAttribute("courses", courses);
            return "egitmen/duyuru";
        }

        List<Long> instructorCourseIds = courses.stream().map(CourseResponse::getId).toList();

        int totalSent = 0;
        Long selectedCourseId = request.getCourseId();

        if (selectedCourseId != null && instructorCourseIds.contains(selectedCourseId)) {
            var enrollments = enrollmentService.findEnrollmentEntitiesByCourse(selectedCourseId);
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
}
