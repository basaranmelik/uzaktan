package com.guzem.uzaktan.dto.response;

import com.guzem.uzaktan.model.EnrollmentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EnrollmentResponse {

    private Long id;
    private Long userId;
    private Long courseId;
    private String courseTitle;
    private String instructorName;
    private String imagePath;
    private EnrollmentStatus status;
    private Integer progressPercentage;
    private LocalDateTime enrollmentDate;
}
