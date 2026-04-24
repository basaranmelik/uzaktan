package com.guzem.uzaktan.dto.request;

import com.guzem.uzaktan.model.course.CorrectOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionUpdateRequest {

    @NotBlank(message = "Soru metni boş olamaz.")
    @Size(min = 5, max = 5000, message = "Soru metni 5-5000 karakter arası olmalıdır.")
    private String questionText;

    @NotBlank(message = "A şıkkı boş olamaz.")
    @Size(max = 500, message = "Şık metni en fazla 500 karakter olabilir.")
    private String optionA;

    @NotBlank(message = "B şıkkı boş olamaz.")
    @Size(max = 500, message = "Şık metni en fazla 500 karakter olabilir.")
    private String optionB;

    @NotBlank(message = "C şıkkı boş olamaz.")
    @Size(max = 500, message = "Şık metni en fazla 500 karakter olabilir.")
    private String optionC;

    @NotBlank(message = "D şıkkı boş olamaz.")
    @Size(max = 500, message = "Şık metni en fazla 500 karakter olabilir.")
    private String optionD;

    @NotBlank(message = "E şıkkı boş olamaz.")
    @Size(max = 500, message = "Şık metni en fazla 500 karakter olabilir.")
    private String optionE;

    @NotNull(message = "Doğru cevap seçilmelidir.")
    private CorrectOption correctOption;

    @Size(max = 5000, message = "Açıklama en fazla 5000 karakter olabilir.")
    private String explanation;
}
