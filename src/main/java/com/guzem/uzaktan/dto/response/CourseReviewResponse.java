package com.guzem.uzaktan.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CourseReviewResponse {
    private Long id;
    private Long courseId;
    private Long userId;
    private String userFullName;
    private Integer rating;
    private String comment;
    private boolean isApproved;
    private LocalDateTime createdAt;
}
