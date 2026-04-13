package com.guzem.uzaktan.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class AssignmentCreateRequest {

    @NotBlank(message = "Ödev başlığı boş olamaz.")
    @Size(min = 3, max = 200, message = "Başlık 3-200 karakter arası olmalıdır.")
    private String title;

    @Size(max = 3000, message = "Açıklama en fazla 3000 karakter olabilir.")
    private String description;

    @NotNull(message = "Son teslim tarihi boş olamaz.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Future(message = "Son teslim tarihi gelecekte olmalıdır.")
    private LocalDateTime dueDate;

    @NotNull(message = "Maksimum puan boş olamaz.")
    @Min(value = 1, message = "Maksimum puan en az 1 olmalıdır.")
    @Max(value = 1000, message = "Maksimum puan en fazla 1000 olabilir.")
    private Integer maxScore;
}
