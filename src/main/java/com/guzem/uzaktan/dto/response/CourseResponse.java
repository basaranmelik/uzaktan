package com.guzem.uzaktan.dto.response;

import com.guzem.uzaktan.model.CourseCategory;
import com.guzem.uzaktan.model.CourseStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private Integer module;
    private CourseCategory category;
    private String categoryDisplayName;
    private CourseStatus status;
    private String instructorName;
    private Long instructorId;
    private Double averageRating;
    private Integer reviewCount;
    private LocalDateTime createdAt;
}
