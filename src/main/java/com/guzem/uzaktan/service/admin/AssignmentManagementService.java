package com.guzem.uzaktan.service.admin;

import com.guzem.uzaktan.dto.request.AssignmentCreateRequest;
import com.guzem.uzaktan.dto.request.AssignmentUpdateRequest;
import com.guzem.uzaktan.dto.request.GradeSubmissionRequest;
import com.guzem.uzaktan.dto.response.AssignmentResponse;
import com.guzem.uzaktan.dto.response.SubmissionResponse;

import java.io.IOException;
import java.util.List;

/**
 * Teacher/admin operations on assignments and submissions.
 */
public interface AssignmentManagementService {

    AssignmentResponse createAssignment(Long courseId, AssignmentCreateRequest request, Long requestingUserId);

    AssignmentResponse updateAssignment(Long assignmentId, AssignmentUpdateRequest request, Long requestingUserId);

    void deleteAssignment(Long assignmentId, Long requestingUserId);

    List<AssignmentResponse> findByCourse(Long courseId);

    AssignmentResponse findById(Long assignmentId, Long requestingUserId);

    List<SubmissionResponse> findSubmissionsByAssignment(Long assignmentId, Long requestingUserId);

    SubmissionResponse gradeSubmission(Long submissionId, GradeSubmissionRequest request, Long requestingUserId);

    byte[] downloadSubmissionsZip(Long assignmentId, Long requestingUserId) throws IOException;

    long countAllAssignments();

    long countPendingSubmissions();

    List<AssignmentResponse> findAllAssignmentsForAdmin();
}
