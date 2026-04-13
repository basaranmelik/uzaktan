package com.guzem.uzaktan.dto.request;

import com.guzem.uzaktan.model.CourseCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CourseCreateRequest {

    @NotBlank(message = "Kurs başlığı boş olamaz.")
    @Size(min = 3, max = 150, message = "Başlık 3-150 karakter arası olmalıdır.")
    private String title;

    @Size(max = 5000, message = "Açıklama en fazla 5000 karakter olabilir.")
    private String description;

    @NotNull(message = "Fiyat boş olamaz.")
    @Positive(message = "Fiyat pozitif olmalıdır.")
    private BigDecimal price;

    @NotNull(message = "Kontenjan boş olamaz.")
    @Positive(message = "Kontenjan pozitif olmalıdır.")
    private Integer quota;

    @NotNull(message = "Başlangıç tarihi boş olamaz.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @NotNull(message = "Bitiş tarihi boş olamaz.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @Positive(message = "Saat pozitif olmalıdır.")
    private Integer hours;

    @NotNull(message = "Modül sayısı boş olamaz.")
    @Positive(message = "Modül sayısı pozitif olmalıdır.")
    private Integer module;

    @NotNull(message = "Kategori boş olamaz.")
    private CourseCategory category;

    @Size(max = 150)
    private String instructorName;

    private Long instructorId;
}
