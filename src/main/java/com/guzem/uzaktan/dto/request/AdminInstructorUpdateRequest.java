package com.guzem.uzaktan.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
public class AdminInstructorUpdateRequest {

    @NotBlank(message = "Ad boş olamaz.")
    @Size(min = 2, max = 100, message = "Ad 2-100 karakter arası olmalıdır.")
    private String firstName;

    @NotBlank(message = "Soyad boş olamaz.")
    @Size(min = 2, max = 100, message = "Soyad 2-100 karakter arası olmalıdır.")
    private String lastName;

    @NotBlank(message = "E-posta adresi zorunludur.")
    @Email(message = "Geçerli bir e-posta giriniz.")
    @Size(max = 254)
    private String email;

    @Pattern(
            regexp = "^(\\+90 \\d{3} \\d{3} \\d{2} \\d{2}|\\+90\\s*)?$",
            message = "Lütfen telefon numaranızı +90 5XX XXX XX XX formatında giriniz."
    )
    private String phone;

    @Past(message = "Doğum tarihi geçmiş bir tarih olmalıdır.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate birthDate;

    @Size(max = 100, message = "Şehir adı en fazla 100 karakter olabilir.")
    private String city;

    @Size(max = 100, message = "İlçe adı en fazla 100 karakter olabilir.")
    private String district;

    @Size(max = 10, message = "Posta kodu en fazla 10 karakter olabilir.")
    private String zipCode;

    @Size(max = 500, message = "Açık adres en fazla 500 karakter olabilir.")
    private String fullAddress;

    @Size(max = 500, message = "Uzmanlık alanı en fazla 500 karakter olabilir.")
    private String expertise;

    @Size(max = 2000, message = "Biyografi en fazla 2000 karakter olabilir.")
    private String bio;

    @Email(message = "Geçerli bir Zoom e-posta adresi giriniz.")
    @Size(max = 254)
    private String zoomEmail;

    private MultipartFile photo;
}
