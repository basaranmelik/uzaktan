package com.guzem.uzaktan.controller.course;

import com.guzem.uzaktan.dto.request.SubmissionCreateRequest;
import com.guzem.uzaktan.dto.response.AssignmentResponse;
import com.guzem.uzaktan.dto.response.SubmissionResponse;
import com.guzem.uzaktan.service.admin.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/odevlerim")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @GetMapping
    public String list(@ModelAttribute("currentUserId") Long currentUserId, Model model) {
        List<AssignmentResponse> assignments = assignmentService.findAssignmentsForStudent(currentUserId);
        
        Map<String, List<AssignmentResponse>> assignmentsByCourse = assignments.stream()
                .collect(Collectors.groupingBy(
                        AssignmentResponse::getCourseTitle,
                        java.util.LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<SubmissionResponse> submissions = assignmentService.findAllSubmissionsForStudent(currentUserId);
        Map<Long, SubmissionResponse> submissionByAssignment = submissions.stream()
                .collect(Collectors.toMap(SubmissionResponse::getAssignmentId, s -> s));
                
        model.addAttribute("assignmentsByCourse", assignmentsByCourse);
        model.addAttribute("assignments", assignments);
        model.addAttribute("submissionByAssignment", submissionByAssignment);
        return "odev/liste";
    }

    @GetMapping("/{assignmentId}")
    public String detail(@PathVariable Long assignmentId,
                         @ModelAttribute("currentUserId") Long currentUserId,
                         Model model) {
        model.addAttribute("assignment", assignmentService.findById(assignmentId, currentUserId));
        Optional<SubmissionResponse> existingSubmission = assignmentService.findSubmission(assignmentId, currentUserId);
        model.addAttribute("submission", existingSubmission.orElse(null));
        model.addAttribute("submissionCreateRequest", new SubmissionCreateRequest());
        return "odev/detay";
    }

    @PostMapping("/{assignmentId}/teslim-et")
    public String submit(@PathVariable Long assignmentId,
                         @ModelAttribute SubmissionCreateRequest request,
                         @RequestParam(value = "file", required = false) MultipartFile file,
                         @ModelAttribute("currentUserId") Long currentUserId,
                         RedirectAttributes redirectAttributes) {
        try {
            assignmentService.submit(assignmentId, currentUserId, request, file);
            redirectAttributes.addFlashAttribute("successMessage", "Ödeviniz başarıyla teslim edildi.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/odevlerim/" + assignmentId;
    }
}
