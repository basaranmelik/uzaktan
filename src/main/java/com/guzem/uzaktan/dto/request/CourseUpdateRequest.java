package com.guzem.uzaktan.dto.request;

import com.guzem.uzaktan.model.course.CourseCategory;
import com.guzem.uzaktan.model.course.CourseLevel;
import com.guzem.uzaktan.model.course.CourseStatus;
import com.guzem.uzaktan.model.course.CourseType;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class CourseUpdateRequest {

    private CourseType courseType;

    @Size(min = 3, max = 150, message = "Başlık 3-150 karakter arası olmalıdır.")
    @jakarta.validation.constraints.NotBlank(message = "Eğitim adı boş olamaz.")
    private String title;

    @Size(max = 5000, message = "Açıklama en fazla 5000 karakter olabilir.")
    @jakarta.validation.constraints.NotBlank(message = "Açıklama boş olamaz.")
    private String description;

    @Positive
    private BigDecimal price;

    @Positive
    private Integer quota;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @Positive
    private Integer hours;

    private CourseCategory category;

    private CourseStatus status;

    @Size(max = 300)
    private String location;

    @Size(max = 300)
    private String courseSchedule;

    private String scheduleDays;

    private String scheduleStartTime;

    private String scheduleEndTime;

    @Size(max = 5000, message = "Manuel müfredat en fazla 5000 karakter olabilir.")
    private String manualCurriculum;

    @Size(max = 150)
    private String instructorName;

    private Long instructorId;

    private List<Long> instructorIds;

    private CourseLevel level;

    // ── UZEM Form Alanları ──────────────────────────────────────────────────

    private String aim;

    @Positive
    private Integer minHours;

    @Positive
    private Integer maxHours;

    @Size(max = 20)
    private String courseVersion;

    @Size(max = 150)
    private String preparedBy;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate preparedDate;

    @Size(max = 150)
    private String reviewedBy;

    @Size(max = 150)
    private String approvedBy;

    private String trainingMethod;

    private String usedMaterials;

    @Size(max = 300)
    private String usedPlatform;

    private String instructorNotes;

    private String targetAudience;
    private String contentTopics;
    private String learningOutcomes;
    private String prerequisites;
    private String assessmentItems;
}
