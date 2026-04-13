package com.guzem.uzaktan.dto.request;

import com.guzem.uzaktan.model.CourseCategory;
import com.guzem.uzaktan.model.CourseStatus;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CourseUpdateRequest {

    @Size(max = 150)
    private String title;

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

    @Positive(message = "Modül sayısı pozitif olmalıdır.")
    private Integer module;

    private CourseCategory category;

    private CourseStatus status;

    @Size(max = 150)
    private String instructorName;

    private Long instructorId;
}
