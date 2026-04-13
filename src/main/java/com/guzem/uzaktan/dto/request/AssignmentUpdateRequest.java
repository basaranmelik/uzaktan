package com.guzem.uzaktan.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class AssignmentUpdateRequest {

    @Size(min = 3, max = 200, message = "Başlık 3-200 karakter arası olmalıdır.")
    private String title;

    @Size(max = 3000, message = "Açıklama en fazla 3000 karakter olabilir.")
    private String description;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dueDate;

    @Min(value = 1, message = "Maksimum puan en az 1 olmalıdır.")
    @Max(value = 1000, message = "Maksimum puan en fazla 1000 olabilir.")
    private Integer maxScore;
}
