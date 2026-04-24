package com.guzem.uzaktan.mapper.course;

import com.guzem.uzaktan.dto.response.EnrollmentResponse;
import com.guzem.uzaktan.model.course.Enrollment;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {

    public EnrollmentResponse toResponse(Enrollment enrollment) {
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .userId(enrollment.getUser().getId())
                .userEmail(enrollment.getUser().getEmail())
                .courseId(enrollment.getCourse().getId())
                .courseTitle(enrollment.getCourse().getTitle())
                .instructorName(enrollment.getCourse().getInstructorName())
                .imagePath(enrollment.getCourse().getImagePath())
                .courseType(enrollment.getCourse().getCourseType())
                .status(enrollment.getStatus())
                .progressPercentage(enrollment.getProgressPercentage())
                .enrollmentDate(enrollment.getEnrollmentDate())
                .startDate(enrollment.getCourse().getStartDate())
                .endDate(enrollment.getCourse().getEndDate())
                .scheduleDays(enrollment.getCourse().getScheduleDays())
                .scheduleStartTime(enrollment.getCourse().getScheduleStartTime())
                .scheduleEndTime(enrollment.getCourse().getScheduleEndTime())
                .hasVideos(enrollment.getCourse().getVideos() != null && !enrollment.getCourse().getVideos().isEmpty())
                .build();
    }
}
