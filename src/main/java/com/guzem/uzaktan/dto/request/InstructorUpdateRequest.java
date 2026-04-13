package com.guzem.uzaktan.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class InstructorUpdateRequest {

    @NotBlank(message = "Eğitmen adı zorunludur")
    private String name;

    private String bio;

    @NotBlank(message = "Uzmanlık alanı zorunludur")
    private String expertise;

    private MultipartFile photo;
}
