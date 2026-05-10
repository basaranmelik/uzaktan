package com.guzem.uzaktan.dto.response;

import com.guzem.uzaktan.dto.AssessmentItem;
import com.guzem.uzaktan.dto.CurriculumModule;
import com.guzem.uzaktan.model.course.CourseLevel;
import com.guzem.uzaktan.model.course.CourseStatus;
import com.guzem.uzaktan.model.course.CourseType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Getter
@Builder
public class CourseResponse {

    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private Integer quota;
    private long enrolledCount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer hours;
    private String categoryDisplayName;
    private CourseStatus status;
    private CourseLevel level;
    private String levelDisplayName;
    private CourseType courseType;
    private String courseTypeDisplayName;
    private String location;
    private String courseSchedule;
    private String scheduleDays;
    private String scheduleStartTime;
    private String scheduleEndTime;
    private String manualCurriculum;
    /** manualCurriculum JSON alanından parse edilmiş modül listesi */
    @Builder.Default
    @lombok.Setter
    private List<CurriculumModule> curriculumModules = Collections.emptyList();
    private String imagePath;
    private String instructorName;
    private Long instructorId;
    @lombok.Setter
    private String instructorImage;
    private List<InstructorResponse> instructors;
    private boolean featured;
    private Double averageRating;
    private Integer reviewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── UZEM Form Alanları ──────────────────────────────────────────────────

    private String aim;
    private Integer minHours;
    private Integer maxHours;
    private String courseVersion;
    private String preparedBy;
    private LocalDate preparedDate;
    private String reviewedBy;
    private String approvedBy;
    private String trainingMethod;
    private String usedMaterials;
    private String usedPlatform;
    private String instructorNotes;

    /** Ham JSON stringler — edit formu pre-fill için */
    private String targetAudience;
    private String contentTopics;
    private String learningOutcomes;
    private String prerequisites;
    private String assessmentItems;

    /** Parse edilmiş listeler — detay sayfası gösterimi için */
    @Builder.Default
    @lombok.Setter
    private List<String> targetAudienceList = Collections.emptyList();

    @Builder.Default
    @lombok.Setter
    private List<String> contentTopicsList = Collections.emptyList();

    @Builder.Default
    @lombok.Setter
    private List<String> learningOutcomesList = Collections.emptyList();

    @Builder.Default
    @lombok.Setter
    private List<String> prerequisitesList = Collections.emptyList();

    @Builder.Default
    @lombok.Setter
    private List<AssessmentItem> assessmentItemsList = Collections.emptyList();
}
