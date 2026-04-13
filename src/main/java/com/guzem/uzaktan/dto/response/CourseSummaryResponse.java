package com.guzem.uzaktan.dto.response;

import com.guzem.uzaktan.model.CourseCategory;
import com.guzem.uzaktan.model.CourseStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class CourseSummaryResponse {

    private Long id;
    private String title;
    private BigDecimal price;
    private CourseCategory category;
    private String categoryDisplayName;
    private CourseStatus status;
    private String instructorName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer hours;
    private boolean enrolled;
}
