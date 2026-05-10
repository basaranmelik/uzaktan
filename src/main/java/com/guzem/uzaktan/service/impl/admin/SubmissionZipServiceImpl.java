package com.guzem.uzaktan.service.impl.admin;

import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.exception.UnauthorizedActionException;
import com.guzem.uzaktan.model.admin.Assignment;
import com.guzem.uzaktan.model.admin.AssignmentSubmission;
import com.guzem.uzaktan.model.common.Role;
import com.guzem.uzaktan.model.common.User;
import com.guzem.uzaktan.repository.admin.AssignmentRepository;
import com.guzem.uzaktan.repository.admin.AssignmentSubmissionRepository;
import com.guzem.uzaktan.repository.user.UserRepository;
import com.guzem.uzaktan.service.admin.SubmissionZipService;
import com.guzem.uzaktan.service.common.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class SubmissionZipServiceImpl implements SubmissionZipService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadSubmissionsZip(Long assignmentId, Long requestingUserId) throws IOException {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Ödev", "id", assignmentId));
        checkTeacherOrAdmin(assignment, requestingUserId);

        List<AssignmentSubmission> submissions = submissionRepository.findByAssignmentIdWithUser(assignmentId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(baos)) {
            for (AssignmentSubmission s : submissions) {
                if (s.getFilePath() == null) continue;
                Path file = fileStorageService.resolve(s.getFilePath());
                if (!Files.exists(file)) continue;
                zip.putNextEntry(new ZipEntry(file.getFileName().toString()));
                Files.copy(file, zip);
                zip.closeEntry();
            }
        }
        return baos.toByteArray();
    }

    private void checkTeacherOrAdmin(Assignment assignment, Long requestingUserId) {
        User user = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "id", requestingUserId));
        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isInstructor = assignment.getCourse().getInstructor() != null
                && assignment.getCourse().getInstructor().getId().equals(requestingUserId);
        if (!isAdmin && !isInstructor) {
            throw new UnauthorizedActionException("Bu işlem için yetkiniz bulunmamaktadır.");
        }
    }
}
