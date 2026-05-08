package com.guzem.uzaktan.mapper.course;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guzem.uzaktan.dto.AssessmentItem;
import com.guzem.uzaktan.dto.CurriculumModule;
import com.guzem.uzaktan.dto.request.CourseCreateRequest;
import com.guzem.uzaktan.dto.request.CourseUpdateRequest;
import com.guzem.uzaktan.dto.response.CourseResponse;
import com.guzem.uzaktan.dto.response.CourseSummaryResponse;
import com.guzem.uzaktan.dto.response.InstructorResponse;
import com.guzem.uzaktan.model.course.Course;
import com.guzem.uzaktan.model.common.User;
import com.guzem.uzaktan.model.course.CourseStatus;
import com.guzem.uzaktan.model.course.CourseType;
import com.guzem.uzaktan.repository.user.UserRepository;
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
    private final UserRepository userRepository;

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
                .imagePath(course.getImagePath())
                .instructorName(course.getInstructorName())
                .instructorId(course.getInstructor() != null ? course.getInstructor().getId() : null)
                .instructors(mapInstructors(course.getInstructors()))
                .averageRating(course.getAverageRating() != null ? course.getAverageRating() : 0.0)
                .reviewCount(course.getReviewCount() != null ? course.getReviewCount() : 0)
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .aim(course.getAim())
                .minHours(course.getMinHours())
                .maxHours(course.getMaxHours())
                .courseVersion(course.getCourseVersion())
                .preparedBy(course.getPreparedBy())
                .preparedDate(course.getPreparedDate())
                .reviewedBy(course.getReviewedBy())
                .approvedBy(course.getApprovedBy())
                .trainingMethod(course.getTrainingMethod())
                .usedMaterials(course.getUsedMaterials())
                .usedPlatform(course.getUsedPlatform())
                .instructorNotes(course.getInstructorNotes())
                .targetAudience(course.getTargetAudience())
                .contentTopics(course.getContentTopics())
                .learningOutcomes(course.getLearningOutcomes())
                .prerequisites(course.getPrerequisites())
                .assessmentItems(course.getAssessmentItems())
                .build();

        response.setCurriculumModules(parseCurriculumModules(course.getManualCurriculum()));
        response.setTargetAudienceList(parseStringList(course.getTargetAudience()));
        response.setContentTopicsList(parseStringList(course.getContentTopics()));
        response.setLearningOutcomesList(parseStringList(course.getLearningOutcomes()));
        response.setPrerequisitesList(parseStringList(course.getPrerequisites()));
        response.setAssessmentItemsList(parseAssessmentItems(course.getAssessmentItems()));
        return response;
    }

    private List<String> parseStringList(String json) {
        if (json == null || json.isBlank() || !json.trim().startsWith("[")) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("JSON parse hatasi (string list): {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<AssessmentItem> parseAssessmentItems(String json) {
        if (json == null || json.isBlank() || !json.trim().startsWith("[")) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<AssessmentItem>>() {});
        } catch (Exception e) {
            log.warn("JSON parse hatasi (assessmentItems): {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /** manualCurriculum JSON string'ini List<CurriculumModule>'e parse eder. */
    public List<CurriculumModule> parseCurriculumModules(String json) {
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
                .category(request.getCategory())
                .level(request.getLevel())
                .location(request.getLocation())
                .courseSchedule(request.getCourseSchedule())
                .scheduleDays(request.getScheduleDays())
                .scheduleStartTime(request.getScheduleStartTime())
                .scheduleEndTime(request.getScheduleEndTime())
                .manualCurriculum(request.getManualCurriculum())
                .instructorName(request.getInstructorName())
                .status(CourseStatus.DRAFT)
                .aim(request.getAim())
                .minHours(request.getMinHours())
                .maxHours(request.getMaxHours())
                .courseVersion(request.getCourseVersion())
                .preparedBy(request.getPreparedBy())
                .preparedDate(request.getPreparedDate())
                .reviewedBy(request.getReviewedBy())
                .approvedBy(request.getApprovedBy())
                .trainingMethod(request.getTrainingMethod())
                .usedMaterials(request.getUsedMaterials())
                .usedPlatform(request.getUsedPlatform())
                .instructorNotes(request.getInstructorNotes())
                .targetAudience(request.getTargetAudience())
                .contentTopics(request.getContentTopics())
                .learningOutcomes(request.getLearningOutcomes())
                .prerequisites(request.getPrerequisites())
                .assessmentItems(request.getAssessmentItems())
                .build();
        
        if (request.getInstructorIds() != null && !request.getInstructorIds().isEmpty()) {
            List<User> instructors = userRepository.findAllById(request.getInstructorIds());
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
        if (request.getCategory() != null)            course.setCategory(request.getCategory());
        if (request.getStatus() != null)              course.setStatus(request.getStatus());
        if (request.getLevel() != null)               course.setLevel(request.getLevel());
        if (request.getLocation() != null)            course.setLocation(request.getLocation());
        if (request.getCourseSchedule() != null)      course.setCourseSchedule(request.getCourseSchedule());
        if (request.getScheduleDays() != null)        course.setScheduleDays(request.getScheduleDays());
        if (request.getScheduleStartTime() != null)   course.setScheduleStartTime(request.getScheduleStartTime());
        if (request.getScheduleEndTime() != null)     course.setScheduleEndTime(request.getScheduleEndTime());
        if (request.getManualCurriculum() != null)    course.setManualCurriculum(request.getManualCurriculum());
        if (request.getInstructorName() != null)      course.setInstructorName(request.getInstructorName());
        if (request.getAim() != null)                 course.setAim(request.getAim());
        if (request.getMinHours() != null)            course.setMinHours(request.getMinHours());
        if (request.getMaxHours() != null)            course.setMaxHours(request.getMaxHours());
        if (request.getCourseVersion() != null)       course.setCourseVersion(request.getCourseVersion());
        if (request.getPreparedBy() != null)          course.setPreparedBy(request.getPreparedBy());
        if (request.getPreparedDate() != null)        course.setPreparedDate(request.getPreparedDate());
        if (request.getReviewedBy() != null)          course.setReviewedBy(request.getReviewedBy());
        if (request.getApprovedBy() != null)          course.setApprovedBy(request.getApprovedBy());
        if (request.getTrainingMethod() != null)      course.setTrainingMethod(request.getTrainingMethod());
        if (request.getUsedMaterials() != null)       course.setUsedMaterials(request.getUsedMaterials());
        if (request.getUsedPlatform() != null)        course.setUsedPlatform(request.getUsedPlatform());
        if (request.getInstructorNotes() != null)     course.setInstructorNotes(request.getInstructorNotes());
        if (request.getTargetAudience() != null)      course.setTargetAudience(request.getTargetAudience());
        if (request.getContentTopics() != null)       course.setContentTopics(request.getContentTopics());
        if (request.getLearningOutcomes() != null)    course.setLearningOutcomes(request.getLearningOutcomes());
        if (request.getPrerequisites() != null)       course.setPrerequisites(request.getPrerequisites());
        if (request.getAssessmentItems() != null)     course.setAssessmentItems(request.getAssessmentItems());

        if (request.getInstructorIds() != null) {
            List<User> instructors = userRepository.findAllById(request.getInstructorIds());
            course.setInstructors(instructors);
        }
    }

    private List<InstructorResponse> mapInstructors(List<User> instructors) {
        if (instructors == null || instructors.isEmpty()) {
            return Collections.emptyList();
        }
        return instructors.stream()
                .map(u -> InstructorResponse.builder()
                        .id(u.getId())
                        .name(u.getFirstName() + " " + u.getLastName())
                        .expertise(u.getSkills())
                        .photoUrl(u.getProfilePictureUrl())
                        .build())
                .collect(Collectors.toList());
    }
}
