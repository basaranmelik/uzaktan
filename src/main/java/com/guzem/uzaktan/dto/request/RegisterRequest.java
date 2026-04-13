package com.guzem.uzaktan.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "E-posta boş olamaz.")
    @Email(message = "Geçerli bir e-posta giriniz.")
    private String email;

    @NotBlank(message = "Şifre boş olamaz.")
    @jakarta.validation.constraints.Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Şifre en az 8 karakter uzunluğunda olmalı ve en az 1 büyük harf, 1 küçük harf, 1 rakam, 1 özel karakter (@$!%*?&) içermelidir."
    )
    private String password;

    @NotBlank(message = "Şifre tekrarı boş olamaz.")
    private String confirmPassword;

    @NotBlank(message = "Ad boş olamaz.")
    @Size(min = 2, max = 100, message = "Ad 2-100 karakter arası olmalıdır.")
    private String firstName;

    @NotBlank(message = "Soyad boş olamaz.")
    @Size(min = 2, max = 100, message = "Soyad 2-100 karakter arası olmalıdır.")
    private String lastName;

    @jakarta.validation.constraints.Pattern(
            regexp = "^\\+90 \\d{3} \\d{3} \\d{2} \\d{2}$",
            message = "Lütfen telefon numaranızı +90 5XX XXX XX XX formatında giriniz."
    )
    private String phoneNumber;

    @NotNull(message = "Doğum tarihi zorunludur.")
    @Past(message = "Doğum tarihi geçmiş bir tarih olmalıdır.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
}
