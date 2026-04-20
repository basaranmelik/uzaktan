package com.guzem.uzaktan.dto.response;

import com.guzem.uzaktan.model.CourseType;
import com.guzem.uzaktan.model.EnrollmentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class EnrollmentResponse {

    private Long id;
    private Long userId;
    private String userEmail;
    private Long courseId;
    private String courseTitle;
    private String instructorName;
    private String imagePath;
    private CourseType courseType;
    private EnrollmentStatus status;
    private Integer progressPercentage;
    private LocalDateTime enrollmentDate;
    
    private LocalDate startDate;
    private LocalDate endDate;
    private String scheduleDays;
    private String scheduleStartTime;
    private String scheduleEndTime;
    private boolean hasVideos;
}
