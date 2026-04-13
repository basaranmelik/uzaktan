package com.guzem.uzaktan.controller;

import com.guzem.uzaktan.dto.response.SubmissionResponse;
import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.exception.UnauthorizedActionException;
import com.guzem.uzaktan.model.Role;
import com.guzem.uzaktan.service.AssignmentService;
import com.guzem.uzaktan.service.FileStorageService;
import com.guzem.uzaktan.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.nio.file.Path;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/dosyalar")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class FileDownloadController {

    private final FileStorageService fileStorageService;
    private final AssignmentService assignmentService;
    private final UserService userService;

    @GetMapping("/teslimler/{submissionId}")
    public ResponseEntity<Resource> downloadSubmissionFile(
            @PathVariable Long submissionId,
            @AuthenticationPrincipal UserDetails principal) {

        SubmissionResponse submission = assignmentService.findSubmissionById(submissionId);

        if (!submission.isHasFile()) {
            throw new ResourceNotFoundException("Dosya", "submissionId", submissionId);
        }

        // Yetki kontrolü: Admin veya kursun öğretmeni veya teslimi yapan öğrenci
        Long currentUserId = userService.findUserIdByEmail(principal.getUsername());
        boolean isOwner = submission.getUserId().equals(currentUserId);
        boolean isAdminOrTeacher = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Role.ADMIN.getAuthority())
                        || a.getAuthority().equals(Role.TEACHER.getAuthority()));

        if (!isOwner && !isAdminOrTeacher) {
            throw new UnauthorizedActionException("Bu dosyayı indirme yetkiniz yok.");
        }

        Path filePath = fileStorageService.resolve(submission.getFilePath());
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                throw new ResourceNotFoundException("Dosya", "path", submission.getFilePath());
            }
            String fileName = submission.getOriginalFileName() != null
                    ? submission.getOriginalFileName()
                    : filePath.getFileName().toString();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Dosya", "path", submission.getFilePath());
        }
    }
}
