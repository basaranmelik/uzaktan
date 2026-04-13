package com.guzem.uzaktan.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AssignmentResponse {

    private Long id;
    private Long courseId;
    private String courseTitle;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private Integer maxScore;
    private long submissionCount;
    private long pendingGradeCount;
    private LocalDateTime createdAt;
}
