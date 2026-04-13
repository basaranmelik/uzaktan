package com.guzem.uzaktan.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseReviewRequest {

    @NotNull(message = "Puan boş bırakılamaz")
    @Min(value = 1, message = "Puan en az 1 olmalıdır")
    @Max(value = 5, message = "Puan en fazla 5 olmalıdır")
    private Integer rating;

    @NotBlank(message = "Yorum boş bırakılamaz")
    private String comment;
}
