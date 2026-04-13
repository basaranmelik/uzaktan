package com.guzem.uzaktan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class InstructorUpdateRequest {

    @NotBlank(message = "Eğitmen adı zorunludur")
    @Size(min = 3, max = 150, message = "Eğitmen adı 3-150 karakter arası olmalıdır.")
    private String name;

    @Size(max = 2000, message = "Biyografi en fazla 2000 karakter olabilir.")
    private String bio;

    @NotBlank(message = "Uzmanlık alanı zorunludur")
    @Size(min = 3, max = 500, message = "Uzmanlık alanı 3-500 karakter arası olmalıdır.")
    private String expertise;

    private MultipartFile photo;
}
