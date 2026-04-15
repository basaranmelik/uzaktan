package com.guzem.uzaktan.dto.response;

import com.guzem.uzaktan.model.SubmissionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SubmissionResponse {

    private Long id;
    private Long assignmentId;
    private String assignmentTitle;
    private Integer assignmentMaxScore;
    private Long courseId;
    private String courseTitle;
    private Long userId;
    private String userFullName;
    private Long instructorId;
    private String textAnswer;
    private String filePath;
    private String originalFileName;
    private boolean hasFile;
    private SubmissionStatus status;
    private String statusDisplayName;
    private Integer score;
    private String feedback;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;
}
