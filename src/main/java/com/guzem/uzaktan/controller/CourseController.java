package com.guzem.uzaktan.controller;

import com.guzem.uzaktan.dto.request.CourseReviewRequest;
import com.guzem.uzaktan.dto.response.CourseResponse;
import com.guzem.uzaktan.dto.response.CourseSummaryResponse;
import com.guzem.uzaktan.dto.response.EnrollmentResponse;
import com.guzem.uzaktan.dto.response.UserResponse;
import com.guzem.uzaktan.model.CourseCategory;
import com.guzem.uzaktan.model.EnrollmentStatus;
import com.guzem.uzaktan.service.CourseReviewService;
import com.guzem.uzaktan.service.CourseService;
import com.guzem.uzaktan.service.CourseVideoService;
import com.guzem.uzaktan.service.EnrollmentService;
import com.guzem.uzaktan.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/egitimler")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final UserService userService;
    private final EnrollmentService enrollmentService;
    private final CourseVideoService courseVideoService;
    private final CourseReviewService courseReviewService;
    private final com.guzem.uzaktan.service.CartService cartService;
    private final com.guzem.uzaktan.service.InstructorService instructorService;

    @GetMapping
    public String listCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(required = false) CourseCategory category,
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal UserDetails principal,
            Model model) {

        Page<CourseSummaryResponse> courses;

        if (keyword != null && !keyword.isBlank()) {
            courses = courseService.search(keyword, page, size);
        } else if (category != null) {
            courses = courseService.findByCategory(category, page, size);
        } else if (principal != null) {
            UserResponse user = userService.findByEmail(principal.getUsername());
            courses = courseService.findPublishedCoursesForUser(user.getId(), page, size);
        } else {
            courses = courseService.findPublishedCourses(page, size);
        }

        model.addAttribute("courses", courses);
        model.addAttribute("categories", CourseCategory.values());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", courses.getTotalPages());
        return "course/list";
    }

    @GetMapping("/{id}")
    public String courseDetail(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails principal,
                               Model model) {
        CourseResponse course = courseService.findById(id);

        if (course.getInstructorName() != null) {
            instructorService.findByName(course.getInstructorName())
                .ifPresent(ins -> course.setInstructorImage(ins.getPhotoUrl()));
        }

        model.addAttribute("course", course);
        // Video başlıklarını göstermek için (kilitli preview olarak)
        model.addAttribute("videos", courseVideoService.findByCourse(id));

        boolean isTeacherOrAdmin = false;
        if (principal != null) {
            UserResponse user = userService.findByEmail(principal.getUsername());
            isTeacherOrAdmin = principal.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_TEACHER"));

            Optional<EnrollmentResponse> enrollmentOpt =
                    enrollmentService.findByUserAndCourse(user.getId(), id);
            boolean enrolled = enrollmentOpt.isPresent();
            boolean isActiveEnrolled = enrollmentOpt
                    .map(e -> e.getStatus() == EnrollmentStatus.ACTIVE).orElse(false);

            // Eğitmen - kendi kursunu görebilir
            if (isTeacherOrAdmin && !principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                if (course.getInstructorId() != null && course.getInstructorId().equals(user.getId())) {
                    isActiveEnrolled = true; // Eğitmen kursunu view-only mode'da görebilir
                }
            }

            model.addAttribute("isEnrolled", enrolled);
            model.addAttribute("isActiveEnrolled", isActiveEnrolled);
            model.addAttribute("enrollmentStatus", enrollmentOpt.map(EnrollmentResponse::getStatus).orElse(null));
            model.addAttribute("currentUser", user);
            model.addAttribute("hasReviewed", courseReviewService.hasUserReviewed(id, user.getId()));
            model.addAttribute("isInCart", cartService.isInCart(user.getId(), id));
            model.addAttribute("isTeacherOrAdmin", isTeacherOrAdmin);
        } else {
            model.addAttribute("hasReviewed", false);
            model.addAttribute("isActiveEnrolled", false);
            model.addAttribute("isInCart", false);
            model.addAttribute("isTeacherOrAdmin", false);
        }

        model.addAttribute("reviews", courseReviewService.getApprovedReviewsByCourse(id));
        model.addAttribute("reviewRequest", new CourseReviewRequest());

        if (course.getInstructorId() != null) {
            model.addAttribute("instructorCourseCount",
                    courseService.countActiveCoursesForInstructor(course.getInstructorId()));
            model.addAttribute("instructorStudentCount",
                    courseService.countTotalStudentsForInstructor(course.getInstructorId()));
        }

        return "course/detail";
    }

    /**
     * Aktif kayıtlı öğrencinin kurs video sayfası.
     * URL: /egitimler/izle/{id}  (id = kurs id'si)
     * Eğitmen kendi kursunu izleyebilir.
     */
    @GetMapping("/izle/{id}")
    @PreAuthorize("isAuthenticated()")
    public String myCoursePlayer(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails principal,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        UserResponse user = userService.findByEmail(principal.getUsername());
        CourseResponse course = courseService.findById(id);

        // Eğitmen/Admin kontrolü
        boolean isTeacherOrAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_TEACHER"));

        boolean isActive = false;
        if (isTeacherOrAdmin) {
            // Admin her kursa erişebilir, eğitmen kendi kursuna
            if (principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                isActive = true;
            } else if (course.getInstructorId() != null && course.getInstructorId().equals(user.getId())) {
                isActive = true; // Eğitmen kendi kursu
            }
        } else {
            // Öğrenci - kayıtlı olmalı
            Optional<EnrollmentResponse> enrollmentOpt =
                    enrollmentService.findByUserAndCourse(user.getId(), id);
            isActive = enrollmentOpt
                    .map(e -> e.getStatus() == EnrollmentStatus.ACTIVE || e.getStatus() == EnrollmentStatus.COMPLETED).orElse(false);
        }

        if (!isActive) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bu eğitime ait içeriklere erişmek için öncelikle kayıt olmanız gerekmektedir.");
            return "redirect:/egitimler/" + id;
        }

        var videos = isTeacherOrAdmin
            ? courseVideoService.findByCourse(id)
            : courseVideoService.findByCourseForStudent(id, user.getId());

        if (videos.isEmpty()) {
            redirectAttributes.addFlashAttribute("infoMessage", "Bu eğitime henüz video eklenmemiştir.");
            return "redirect:/egitimler/" + id;
        }

        var firstUnwatched = videos.stream()
                .filter(v -> !v.isWatched())
                .findFirst()
                .orElse(videos.get(0)); // Hepsi izlendiyse ilkine git

        return "redirect:/videolar/" + firstUnwatched.getId();
    }

    @PostMapping("/{id}/yorum")
    @PreAuthorize("isAuthenticated()")
    public String submitReview(@PathVariable Long id,
                               @Valid @ModelAttribute("reviewRequest") CourseReviewRequest request,
                               @AuthenticationPrincipal UserDetails principal,
                               RedirectAttributes redirectAttributes) {
        UserResponse user = userService.findByEmail(principal.getUsername());

        Optional<EnrollmentResponse> enrollmentOpt =
                enrollmentService.findByUserAndCourse(user.getId(), id);
        boolean isActive = enrollmentOpt
                .map(e -> e.getStatus() == EnrollmentStatus.ACTIVE).orElse(false);

        if (!isActive) {
            redirectAttributes.addFlashAttribute("errorMessage", "Yorum yapabilmek için kursa kayıtlı olmalısınız.");
            return "redirect:/egitimler/" + id;
        }

        try {
            courseReviewService.addReview(id, user.getId(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Yorumunuz alındı, yönetici onayından sonra yayınlanacaktır.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/egitimler/" + id;
    }
}
