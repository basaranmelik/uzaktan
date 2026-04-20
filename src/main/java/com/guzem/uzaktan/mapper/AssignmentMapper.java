package com.guzem.uzaktan.mapper;

import com.guzem.uzaktan.dto.request.AssignmentCreateRequest;
import com.guzem.uzaktan.dto.response.AssignmentResponse;
import com.guzem.uzaktan.dto.response.SubmissionResponse;
import com.guzem.uzaktan.model.Assignment;
import com.guzem.uzaktan.model.AssignmentSubmission;
import com.guzem.uzaktan.model.Course;
import org.springframework.stereotype.Component;

@Component
public class AssignmentMapper {

    public AssignmentResponse toResponse(Assignment a, long submissionCount, long pendingCount) {
        return AssignmentResponse.builder()
                .id(a.getId())
                .courseId(a.getCourse().getId())
                .courseTitle(a.getCourse().getTitle())
                .title(a.getTitle())
                .description(a.getDescription())
                .dueDate(a.getDueDate())
                .maxScore(a.getMaxScore())
                .submissionCount(submissionCount)
                .pendingGradeCount(pendingCount)
                .createdAt(a.getCreatedAt())
                .build();
    }

    public Assignment toEntity(AssignmentCreateRequest req, Course course) {
        return Assignment.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .dueDate(req.getDueDate())
                .maxScore(req.getMaxScore())
                .course(course)
                .build();
    }

    public SubmissionResponse toSubmissionResponse(AssignmentSubmission s) {
        return SubmissionResponse.builder()
                .id(s.getId())
                .assignmentId(s.getAssignment().getId())
                .assignmentTitle(s.getAssignment().getTitle())
                .assignmentMaxScore(s.getAssignment().getMaxScore())
                .courseId(s.getAssignment().getCourse().getId())
                .courseTitle(s.getAssignment().getCourse().getTitle())
                .instructorId(s.getAssignment().getCourse().getInstructor() != null
                        ? s.getAssignment().getCourse().getInstructor().getId() : null)
                .userId(s.getUser().getId())
                .userFullName(s.getUser().getFirstName() + " " + s.getUser().getLastName())
                .userEmail(s.getUser().getEmail())
                .textAnswer(s.getTextAnswer())
                .filePath(s.getFilePath())
                .originalFileName(s.getOriginalFileName())
                .hasFile(s.getFilePath() != null && !s.getFilePath().isBlank())
                .status(s.getStatus())
                .statusDisplayName(s.getStatus().getDisplayName())
                .score(s.getScore())
                .feedback(s.getFeedback())
                .submittedAt(s.getSubmittedAt())
                .gradedAt(s.getGradedAt())
                .build();
    }
}
