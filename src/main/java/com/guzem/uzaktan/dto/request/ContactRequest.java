package com.guzem.uzaktan.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactRequest {

    @NotBlank(message = "Ad zorunludur")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Soyad zorunludur")
    @Size(max = 100)
    private String lastName;

    @NotBlank(message = "E-posta zorunludur")
    @Email(message = "Geçerli bir e-posta adresi giriniz")
    @Size(max = 255)
    private String email;

    @NotBlank(message = "Telefon numarası zorunludur")
    @Size(max = 20)
    private String phone;

    @NotBlank(message = "Konu zorunludur")
    private String topic;

    @NotBlank(message = "Mesaj zorunludur")
    @Size(max = 2000)
    private String message;
}
