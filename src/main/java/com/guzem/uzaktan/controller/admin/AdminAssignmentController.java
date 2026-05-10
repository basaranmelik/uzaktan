package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.dto.response.AssignmentResponse;
import com.guzem.uzaktan.dto.response.SubmissionResponse;
import com.guzem.uzaktan.service.admin.AssignmentService;
import com.guzem.uzaktan.service.admin.SubmissionZipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin/odevler")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAssignmentController {

    private final AssignmentService assignmentService;
    private final SubmissionZipService submissionZipService;

    @GetMapping
    public String assignments(Model model) {
        List<AssignmentResponse> assignments = assignmentService.findAllAssignmentsForAdmin();
        model.addAttribute("assignments", assignments);
        return "admin/assignments";
    }

    @GetMapping("/{id}/teslimler")
    public String viewSubmissions(@PathVariable Long id, Model model,
                                  @org.springframework.web.bind.annotation.ModelAttribute("currentUserId") Long currentUserId) {
        AssignmentResponse assignment = assignmentService.findById(id, currentUserId);
        List<SubmissionResponse> submissions = assignmentService.findSubmissionsByAssignment(id, currentUserId);
        model.addAttribute("assignment", assignment);
        model.addAttribute("submissions", submissions);
        return "admin/assignment-submissions";
    }

    @GetMapping("/{id}/indir-zip")
    public ResponseEntity<byte[]> downloadSubmissions(@PathVariable Long id,
            @org.springframework.web.bind.annotation.ModelAttribute("currentUserId") Long currentUserId) throws IOException {
        byte[] zipBytes = submissionZipService.downloadSubmissionsZip(id, currentUserId);
        AssignmentResponse assignment = assignmentService.findById(id, currentUserId);
        String filename = "Odev_" + assignment.getId() + "_Teslimler.zip";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(zipBytes);
    }
}
