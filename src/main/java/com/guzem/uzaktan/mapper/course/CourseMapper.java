package com.guzem.uzaktan.mapper.course;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guzem.uzaktan.dto.CurriculumModule;
import com.guzem.uzaktan.dto.request.CourseCreateRequest;
import com.guzem.uzaktan.dto.request.CourseUpdateRequest;
import com.guzem.uzaktan.dto.response.CourseResponse;
import com.guzem.uzaktan.dto.response.CourseSummaryResponse;
import com.guzem.uzaktan.dto.response.InstructorResponse;
import com.guzem.uzaktan.model.course.Course;
import com.guzem.uzaktan.model.course.CourseStatus;
import com.guzem.uzaktan.model.course.CourseType;
import com.guzem.uzaktan.model.instructor.Instructor;
import com.guzem.uzaktan.repository.instructor.InstructorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseMapper {

    private final ObjectMapper objectMapper;
    private final InstructorRepository instructorRepository;

    public CourseResponse toResponse(Course course, long enrolledCount) {
        CourseResponse response = CourseResponse.builder()
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
                .categoryDisplayName(course.getCategory() != null ? course.getCategory().getDisplayName() : null)
                .status(course.getStatus())
                .level(course.getLevel())
                .levelDisplayName(course.getLevel() != null ? course.getLevel().getDisplayName() : null)
                .courseType(course.getCourseType())
                .courseTypeDisplayName(course.getCourseType() != null ? course.getCourseType().getDisplayName() : null)
                .location(course.getLocation())
                .courseSchedule(course.getCourseSchedule())
                .scheduleDays(course.getScheduleDays())
                .scheduleStartTime(course.getScheduleStartTime())
                .scheduleEndTime(course.getScheduleEndTime())
                .manualCurriculum(course.getManualCurriculum())
                .certificateDeadline(course.getCertificateDeadline())
                .imagePath(course.getImagePath())
                .instructorName(course.getInstructorName())
                .instructorId(course.getInstructor() != null ? course.getInstructor().getId() : null)
                .instructors(mapInstructors(course.getInstructors()))
                .averageRating(course.getAverageRating() != null ? course.getAverageRating() : 0.0)
                .reviewCount(course.getReviewCount() != null ? course.getReviewCount() : 0)
                .createdAt(course.getCreatedAt())
                .build();

        response.setCurriculumModules(parseCurriculumModules(course.getManualCurriculum()));
        return response;
    }

    /** manualCurriculum JSON string'ini List<CurriculumModule>'e parse eder. */
    private List<CurriculumModule> parseCurriculumModules(String json) {
        if (json == null || json.isBlank() || !json.trim().startsWith("[")) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<CurriculumModule>>() {});
        } catch (Exception e) {
            log.warn("manualCurriculum JSON parse hatasi: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public CourseSummaryResponse toSummaryResponse(Course course, boolean enrolled, long enrolledCount) {
        return CourseSummaryResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .price(course.getPrice())
                .category(course.getCategory())
                .categoryDisplayName(course.getCategory() != null ? course.getCategory().getDisplayName() : null)
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
        Course course = Course.builder()
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
                .scheduleDays(request.getScheduleDays())
                .scheduleStartTime(request.getScheduleStartTime())
                .scheduleEndTime(request.getScheduleEndTime())
                .manualCurriculum(request.getManualCurriculum())
                .certificateDeadline(request.getCertificateDeadline())
                .instructorName(request.getInstructorName())
                .status(CourseStatus.DRAFT)
                .build();
        
        if (request.getInstructorIds() != null && !request.getInstructorIds().isEmpty()) {
            List<Instructor> instructors = instructorRepository.findAllById(request.getInstructorIds());
            course.setInstructors(instructors);
        }
        
        return course;
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
        if (request.getScheduleDays() != null)        course.setScheduleDays(request.getScheduleDays());
        if (request.getScheduleStartTime() != null)   course.setScheduleStartTime(request.getScheduleStartTime());
        if (request.getScheduleEndTime() != null)     course.setScheduleEndTime(request.getScheduleEndTime());
        if (request.getManualCurriculum() != null)    course.setManualCurriculum(request.getManualCurriculum());
        if (request.getCertificateDeadline() != null) course.setCertificateDeadline(request.getCertificateDeadline());
        if (request.getInstructorName() != null)      course.setInstructorName(request.getInstructorName());
        
        if (request.getInstructorIds() != null) {
            List<Instructor> instructors = instructorRepository.findAllById(request.getInstructorIds());
            course.setInstructors(instructors);
        }
    }

    private List<InstructorResponse> mapInstructors(List<Instructor> instructors) {
        if (instructors == null || instructors.isEmpty()) {
            return Collections.emptyList();
        }
        return instructors.stream()
                .map(i -> InstructorResponse.builder()
                        .id(i.getId())
                        .name(i.getName())
                        .expertise(i.getExpertise())
                        .photoUrl(i.getPhotoUrl())
                        .createdAt(i.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
