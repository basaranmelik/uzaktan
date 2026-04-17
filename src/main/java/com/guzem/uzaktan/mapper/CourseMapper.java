package com.guzem.uzaktan.mapper;

import com.guzem.uzaktan.dto.request.CourseCreateRequest;
import com.guzem.uzaktan.dto.request.CourseUpdateRequest;
import com.guzem.uzaktan.dto.response.CourseResponse;
import com.guzem.uzaktan.dto.response.CourseSummaryResponse;
import com.guzem.uzaktan.model.Course;
import com.guzem.uzaktan.model.CourseStatus;
import com.guzem.uzaktan.model.CourseType;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

    public CourseResponse toResponse(Course course, long enrolledCount) {
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .price(course.getPrice())
                .quota(course.getQuota())
                .enrolledCount(enrolledCount)
                .startDate(course.getStartDate())
                .endDate(course.getEndDate())
                .hours(course.getHours())
                .module(course.getModule())
                .category(course.getCategory())
                .categoryDisplayName(course.getCategory().getDisplayName())
                .status(course.getStatus())
                .level(course.getLevel())
                .levelDisplayName(course.getLevel() != null ? course.getLevel().getDisplayName() : null)
                .courseType(course.getCourseType())
                .courseTypeDisplayName(course.getCourseType() != null ? course.getCourseType().getDisplayName() : null)
                .location(course.getLocation())
                .courseSchedule(course.getCourseSchedule())
                .manualCurriculum(course.getManualCurriculum())
                .certificateDeadline(course.getCertificateDeadline())
                .imagePath(course.getImagePath())
                .instructorName(course.getInstructorName())
                .instructorId(course.getInstructor() != null ? course.getInstructor().getId() : null)
                .averageRating(course.getAverageRating() != null ? course.getAverageRating() : 0.0)
                .reviewCount(course.getReviewCount() != null ? course.getReviewCount() : 0)
                .createdAt(course.getCreatedAt())
                .build();
    }

    public CourseSummaryResponse toSummaryResponse(Course course, boolean enrolled, long enrolledCount) {
        return CourseSummaryResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .price(course.getPrice())
                .category(course.getCategory())
                .categoryDisplayName(course.getCategory().getDisplayName())
                .status(course.getStatus())
                .courseType(course.getCourseType())
                .courseTypeDisplayName(course.getCourseType() != null ? course.getCourseType().getDisplayName() : null)
                .imagePath(course.getImagePath())
                .instructorName(course.getInstructorName())
                .startDate(course.getStartDate())
                .endDate(course.getEndDate())
                .hours(course.getHours())
                .enrolled(enrolled)
                .quota(course.getQuota())
                .enrolledCount(enrolledCount)
                .build();
    }

    public Course toEntity(CourseCreateRequest request) {
        return Course.builder()
                .courseType(request.getCourseType() != null ? request.getCourseType() : CourseType.ONLINE)
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .quota(request.getQuota())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .hours(request.getHours())
                .module(request.getModule())
                .category(request.getCategory())
                .level(request.getLevel())
                .location(request.getLocation())
                .courseSchedule(request.getCourseSchedule())
                .manualCurriculum(request.getManualCurriculum())
                .certificateDeadline(request.getCertificateDeadline())
                .instructorName(request.getInstructorName())
                .status(CourseStatus.DRAFT)
                .build();
    }

    public void updateEntity(Course course, CourseUpdateRequest request) {
        if (request.getCourseType() != null)          course.setCourseType(request.getCourseType());
        if (request.getTitle() != null)               course.setTitle(request.getTitle());
        if (request.getDescription() != null)         course.setDescription(request.getDescription());
        if (request.getPrice() != null)               course.setPrice(request.getPrice());
        if (request.getQuota() != null)               course.setQuota(request.getQuota());
        if (request.getStartDate() != null)           course.setStartDate(request.getStartDate());
        if (request.getEndDate() != null)             course.setEndDate(request.getEndDate());
        if (request.getHours() != null)               course.setHours(request.getHours());
        if (request.getModule() != null)              course.setModule(request.getModule());
        if (request.getCategory() != null)            course.setCategory(request.getCategory());
        if (request.getStatus() != null)              course.setStatus(request.getStatus());
        if (request.getLevel() != null)               course.setLevel(request.getLevel());
        if (request.getLocation() != null)            course.setLocation(request.getLocation());
        if (request.getCourseSchedule() != null)      course.setCourseSchedule(request.getCourseSchedule());
        if (request.getManualCurriculum() != null)    course.setManualCurriculum(request.getManualCurriculum());
        if (request.getCertificateDeadline() != null) course.setCertificateDeadline(request.getCertificateDeadline());
        if (request.getInstructorName() != null)      course.setInstructorName(request.getInstructorName());
    }
}
