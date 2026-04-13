package com.guzem.uzaktan.controller;

import com.guzem.uzaktan.dto.request.AssignmentCreateRequest;
import com.guzem.uzaktan.dto.request.AssignmentUpdateRequest;
import com.guzem.uzaktan.dto.request.GradeSubmissionRequest;
import com.guzem.uzaktan.dto.response.AssignmentResponse;
import com.guzem.uzaktan.service.AssignmentService;
import com.guzem.uzaktan.service.CourseService;
import com.guzem.uzaktan.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/egitmen")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
public class TeacherController {

    private final CourseService courseService;
    private final AssignmentService assignmentService;
    private final UserService userService;

    // ---- Panel ----

    @GetMapping("/panel")
    public String panel(@AuthenticationPrincipal UserDetails principal, Model model) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        var courses = courseService.findByInstructor(userId);
        model.addAttribute("courses", courses);
        model.addAttribute("courseCount", courses.size());
        model.addAttribute("totalStudents", courseService.countTotalStudentsForInstructor(userId));
        model.addAttribute("activeCourses", courseService.countActiveCoursesForInstructor(userId));
        return "egitmen/panel";
    }

    @GetMapping("/kurslarim")
    public String myCourses(@AuthenticationPrincipal UserDetails principal, Model model) {
        model.addAttribute("courses", courseService.findByInstructor(userService.findUserIdByEmail(principal.getUsername())));
        return "egitmen/kurslarim";
    }

    // ---- Ödev Yönetimi ----

    @GetMapping("/kurslarim/{courseId}/odevler")
    public String courseAssignments(@PathVariable Long courseId, Model model) {
        model.addAttribute("course", courseService.findById(courseId));
        model.addAttribute("assignments", assignmentService.findByCourse(courseId));
        return "egitmen/kurs-odevleri";
    }

    @GetMapping("/kurslarim/{courseId}/odevler/yeni")
    public String newAssignmentForm(@PathVariable Long courseId, Model model) {
        model.addAttribute("course", courseService.findById(courseId));
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
    public String editAssignmentForm(@PathVariable Long id, Model model) {
        var assignment = assignmentService.findById(id);
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
            model.addAttribute("assignment", assignmentService.findById(id));
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
        AssignmentResponse assignment = assignmentService.findById(id);
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
        model.addAttribute("assignment", assignmentService.findById(id));
        model.addAttribute("submissions", assignmentService.findSubmissionsByAssignment(id, userService.findUserIdByEmail(principal.getUsername())));
        return "egitmen/teslimler";
    }

    @GetMapping("/teslimler/{submissionId}/notlandir")
    public String gradeForm(@PathVariable Long submissionId, Model model) {
        var submission = assignmentService.findSubmissionById(submissionId);
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
            model.addAttribute("submission", assignmentService.findSubmissionById(submissionId));
            return "egitmen/notlandir";
        }
        var graded = assignmentService.gradeSubmission(submissionId, request, userService.findUserIdByEmail(principal.getUsername()));
        redirectAttributes.addFlashAttribute("successMessage", "Not kaydedildi.");
        return "redirect:/egitmen/odevler/" + graded.getAssignmentId() + "/teslimler";
    }
}
