package com.guzem.uzaktan.service;

import com.guzem.uzaktan.dto.request.AssignmentCreateRequest;
import com.guzem.uzaktan.dto.request.AssignmentUpdateRequest;
import com.guzem.uzaktan.dto.request.GradeSubmissionRequest;
import com.guzem.uzaktan.dto.request.SubmissionCreateRequest;
import com.guzem.uzaktan.dto.response.AssignmentResponse;
import com.guzem.uzaktan.dto.response.SubmissionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface AssignmentService {

    // --- Öğretmen / Admin işlemleri ---

    AssignmentResponse createAssignment(Long courseId, AssignmentCreateRequest request, Long requestingUserId);

    AssignmentResponse updateAssignment(Long assignmentId, AssignmentUpdateRequest request, Long requestingUserId);

    void deleteAssignment(Long assignmentId, Long requestingUserId);

    List<AssignmentResponse> findByCourse(Long courseId);

    AssignmentResponse findById(Long assignmentId);

    List<SubmissionResponse> findSubmissionsByAssignment(Long assignmentId, Long requestingUserId);

    SubmissionResponse gradeSubmission(Long submissionId, GradeSubmissionRequest request, Long requestingUserId);

    // --- Öğrenci işlemleri ---

    List<AssignmentResponse> findAssignmentsForStudent(Long userId);

    SubmissionResponse submit(Long assignmentId, Long userId,
                              SubmissionCreateRequest request,
                              MultipartFile file);

    Optional<SubmissionResponse> findSubmission(Long assignmentId, Long userId);

    SubmissionResponse findSubmissionById(Long submissionId);

    List<SubmissionResponse> findAllSubmissionsForStudent(Long userId);

    List<AssignmentResponse> findPendingAssignmentsForStudent(Long userId);

    // --- Admin istatistikleri ---

    long countAllAssignments();

    long countPendingSubmissions();
}
