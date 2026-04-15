package com.guzem.uzaktan.controller;

import com.guzem.uzaktan.dto.request.SubmissionCreateRequest;
import com.guzem.uzaktan.dto.response.AssignmentResponse;
import com.guzem.uzaktan.dto.response.SubmissionResponse;
import com.guzem.uzaktan.service.AssignmentService;
import com.guzem.uzaktan.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/odevlerim")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final UserService userService;

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails principal, Model model) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        List<AssignmentResponse> assignments = assignmentService.findAssignmentsForStudent(userId);
        
        Map<String, List<AssignmentResponse>> assignmentsByCourse = assignments.stream()
                .collect(Collectors.groupingBy(
                        AssignmentResponse::getCourseTitle,
                        java.util.LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<SubmissionResponse> submissions = assignmentService.findAllSubmissionsForStudent(userId);
        Map<Long, SubmissionResponse> submissionByAssignment = submissions.stream()
                .collect(Collectors.toMap(SubmissionResponse::getAssignmentId, s -> s));
                
        model.addAttribute("assignmentsByCourse", assignmentsByCourse);
        model.addAttribute("assignments", assignments);
        model.addAttribute("submissionByAssignment", submissionByAssignment);
        return "odev/liste";
    }

    @GetMapping("/{assignmentId}")
    public String detail(@PathVariable Long assignmentId,
                         @AuthenticationPrincipal UserDetails principal,
                         Model model) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        model.addAttribute("assignment", assignmentService.findById(assignmentId, userId));
        Optional<SubmissionResponse> existingSubmission = assignmentService.findSubmission(assignmentId, userId);
        model.addAttribute("submission", existingSubmission.orElse(null));
        model.addAttribute("submissionCreateRequest", new SubmissionCreateRequest());
        return "odev/detay";
    }

    @PostMapping("/{assignmentId}/teslim-et")
    public String submit(@PathVariable Long assignmentId,
                         @ModelAttribute SubmissionCreateRequest request,
                         @RequestParam(value = "file", required = false) MultipartFile file,
                         @AuthenticationPrincipal UserDetails principal,
                         RedirectAttributes redirectAttributes) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        try {
            assignmentService.submit(assignmentId, userId, request, file);
            redirectAttributes.addFlashAttribute("successMessage", "Ödeviniz başarıyla teslim edildi.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/odevlerim/" + assignmentId;
    }

    @PostMapping("/{assignmentId}/guncelle")
    public String update(@PathVariable Long assignmentId,
                         @ModelAttribute SubmissionCreateRequest request,
                         @RequestParam(value = "file", required = false) MultipartFile file,
                         @AuthenticationPrincipal UserDetails principal,
                         RedirectAttributes redirectAttributes) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        try {
            assignmentService.updateSubmission(assignmentId, userId, request, file);
            redirectAttributes.addFlashAttribute("successMessage", "Tesliminiz güncellendi.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/odevlerim/" + assignmentId;
    }
}
