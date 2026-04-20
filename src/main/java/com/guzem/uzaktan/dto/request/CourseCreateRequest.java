package com.guzem.uzaktan.dto.request;

import com.guzem.uzaktan.model.CourseCategory;
import com.guzem.uzaktan.model.CourseLevel;
import com.guzem.uzaktan.model.CourseType;
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

    @NotNull(message = "Kurs türü boş olamaz.")
    private CourseType courseType;

    @NotBlank(message = "Kurs başlığı boş olamaz.")
    @Size(min = 3, max = 150, message = "Başlık 3-150 karakter arası olmalıdır.")
    private String title;

    @Size(max = 5000, message = "Açıklama en fazla 5000 karakter olabilir.")
    private String description;

    @NotNull(message = "Fiyat boş olamaz.")
    @Positive(message = "Fiyat pozitif olmalıdır.")
    private BigDecimal price;

    // Tür bazlı validasyon service katmanında yapılır
    @Positive(message = "Kontenjan pozitif olmalıdır.")
    private Integer quota;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @Positive(message = "Saat pozitif olmalıdır.")
    private Integer hours;

    @Positive(message = "Modül sayısı pozitif olmalıdır.")
    private Integer module;

    @NotNull(message = "Kategori boş olamaz.")
    private CourseCategory category;

    @Size(max = 300, message = "Konum en fazla 300 karakter olabilir.")
    private String location;

    @Size(max = 300, message = "Ders programı en fazla 300 karakter olabilir.")
    private String courseSchedule;

    private String scheduleDays;

    private String scheduleStartTime;

    private String scheduleEndTime;

    @Size(max = 5000, message = "Manuel müfredat en fazla 5000 karakter olabilir.")
    private String manualCurriculum;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate certificateDeadline;

    @Size(max = 150)
    private String instructorName;

    private Long instructorId;

    private CourseLevel level;
}
