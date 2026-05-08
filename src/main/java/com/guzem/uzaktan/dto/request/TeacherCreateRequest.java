package com.guzem.uzaktan.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class TeacherCreateRequest {

    @NotBlank(message = "E-posta adresi zorunludur.")
    @Email(message = "Geçerli bir e-posta adresi giriniz.")
    private String email;

    @NotBlank(message = "Ad zorunludur.")
    @Size(min = 2, max = 50)
    private String firstName;

    @NotBlank(message = "Soyad zorunludur.")
    @Size(min = 2, max = 50)
    private String lastName;

    @Size(max = 500)
    private String expertise;

    private String bio;

    @Email(message = "Geçerli bir Zoom e-posta adresi giriniz.")
    private String zoomEmail;

    private MultipartFile photo;
}
