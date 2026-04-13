package com.guzem.uzaktan.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateRequest {

    @NotBlank(message = "Ad boş olamaz.")
    private String firstName;

    @NotBlank(message = "Soyad boş olamaz.")
    private String lastName;

    @Email(message = "Geçerli bir e-posta giriniz.")
    private String email;

    @jakarta.validation.constraints.Pattern(
            regexp = "^(\\+90 \\d{3} \\d{3} \\d{2} \\d{2})?$",
            message = "Lütfen telefon numaranızı +90 5XX XXX XX XX formatında giriniz."
    )
    private String phoneNumber;

    private String city;

    private String district;

    private String zipCode;

    private String fullAddress;
}
