package com.guzem.uzaktan.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GradeSubmissionRequest {

    @NotNull(message = "Puan boş olamaz.")
    @Min(value = 0, message = "Puan negatif olamaz.")
    @Max(value = 1000, message = "Puan en fazla 1000 olabilir.")
    private Integer score;

    @Size(max = 2000, message = "Geri bildirim en fazla 2000 karakter olabilir.")
    private String feedback;
}
