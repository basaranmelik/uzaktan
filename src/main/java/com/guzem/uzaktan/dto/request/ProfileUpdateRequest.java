package com.guzem.uzaktan.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class ProfileUpdateRequest {

    @NotBlank(message = "Ad boş olamaz.")
    @Size(min = 2, max = 100, message = "Ad 2-100 karakter arası olmalıdır.")
    private String firstName;

    @NotBlank(message = "Soyad boş olamaz.")
    @Size(min = 2, max = 100, message = "Soyad 2-100 karakter arası olmalıdır.")
    private String lastName;

    @Email(message = "Geçerli bir e-posta giriniz.")
    private String email;

    @Past(message = "Doğum tarihi geçmiş bir tarih olmalıdır.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate birthDate;

    @jakarta.validation.constraints.Pattern(
            regexp = "^(\\+90 \\d{3} \\d{3} \\d{2} \\d{2}|\\+90\\s*)?$",
            message = "Lütfen telefon numaranızı +90 5XX XXX XX XX formatında giriniz."
    )
    private String phoneNumber;

    @Size(max = 100, message = "Şehir adı en fazla 100 karakter olabilir.")
    private String city;

    @Size(max = 100, message = "İlçe adı en fazla 100 karakter olabilir.")
    private String district;

    @Size(max = 10, message = "Posta kodu en fazla 10 karakter olabilir.")
    private String zipCode;

    @Size(max = 500, message = "Adres en fazla 500 karakter olabilir.")
    private String fullAddress;
}
