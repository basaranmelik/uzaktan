package com.guzem.uzaktan.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmissionCreateRequest {

    @Size(max = 10000, message = "Metin cevabı 10000 karakter ile sınırlıdır.")
    private String textAnswer;
}
