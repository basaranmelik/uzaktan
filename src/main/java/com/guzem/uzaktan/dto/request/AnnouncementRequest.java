package com.guzem.uzaktan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AnnouncementRequest {

    @NotNull(message = "Lütfen bir kurs seçin")
    private Long courseId;

    @NotBlank(message = "Duyuru başlığı zorunludur")
    @Size(max = 200, message = "Başlık en fazla 200 karakter olabilir")
    private String subject;

    @NotBlank(message = "Duyuru mesajı zorunludur")
    @Size(max = 5000, message = "Mesaj en fazla 5000 karakter olabilir")
    private String message;
}
