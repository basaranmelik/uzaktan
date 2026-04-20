package com.guzem.uzaktan.service;

import com.guzem.uzaktan.dto.request.SubmissionCreateRequest;
import com.guzem.uzaktan.dto.response.AssignmentResponse;
import com.guzem.uzaktan.dto.response.SubmissionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * Student-facing submission operations.
 */
public interface SubmissionService {

    List<AssignmentResponse> findAssignmentsForStudent(Long userId);

    SubmissionResponse submit(Long assignmentId, Long userId,
                              SubmissionCreateRequest request,
                              MultipartFile file);

    Optional<SubmissionResponse> findSubmission(Long assignmentId, Long userId);

    SubmissionResponse findSubmissionById(Long submissionId, Long requestingUserId);

    List<SubmissionResponse> findAllSubmissionsForStudent(Long userId);

    List<AssignmentResponse> findPendingAssignmentsForStudent(Long userId);

    List<AssignmentResponse> findOverdueAssignmentsForStudent(Long userId);

    List<com.guzem.uzaktan.dto.response.UserResponse> findNotSubmittedStudents(Long courseId, Long assignmentId);
}
