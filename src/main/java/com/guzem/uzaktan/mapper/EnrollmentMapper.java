package com.guzem.uzaktan.mapper;

import com.guzem.uzaktan.dto.response.EnrollmentResponse;
import com.guzem.uzaktan.model.Enrollment;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {

    public EnrollmentResponse toResponse(Enrollment enrollment) {
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .userId(enrollment.getUser().getId())
                .courseId(enrollment.getCourse().getId())
                .courseTitle(enrollment.getCourse().getTitle())
                .instructorName(enrollment.getCourse().getInstructorName())
                .imagePath(enrollment.getCourse().getImagePath())
                .status(enrollment.getStatus())
                .progressPercentage(enrollment.getProgressPercentage())
                .enrollmentDate(enrollment.getEnrollmentDate())
                .build();
    }
}
