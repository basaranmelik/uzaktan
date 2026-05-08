package com.guzem.uzaktan.dto.request;

import com.guzem.uzaktan.model.course.CourseCategory;
import com.guzem.uzaktan.model.course.CourseLevel;
import com.guzem.uzaktan.model.course.CourseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class CourseCreateRequest {

    @NotNull(message = "Kurs türü boş olamaz.")
    private CourseType courseType;

    @NotBlank(message = "Kurs başlığı boş olamaz.")
    @Size(min = 3, max = 150, message = "Başlık 3-150 karakter arası olmalıdır.")
    private String title;

    @NotBlank(message = "Açıklama boş olamaz.")
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

    @Size(max = 150)
    private String instructorName;

    @NotNull(message = "Eğitmen seçimi zorunludur.")
    private Long instructorId;

    private java.util.List<Long> instructorIds;

    private CourseLevel level;

    // ── UZEM Form Alanları ──────────────────────────────────────────────────

    private String aim;

    @Positive(message = "Minimum süre pozitif olmalıdır.")
    private Integer minHours;

    @Positive(message = "Maksimum süre pozitif olmalıdır.")
    private Integer maxHours;

    @Size(max = 20)
    private String courseVersion;

    @Size(max = 150)
    private String preparedBy;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate preparedDate;

    @Size(max = 150)
    private String reviewedBy;

    @Size(max = 150)
    private String approvedBy;

    private String trainingMethod;

    private String usedMaterials;

    @Size(max = 300)
    private String usedPlatform;

    private String instructorNotes;

    /** JSON string — Hedef Kitle */
    private String targetAudience;

    /** JSON string — Eğitim İçeriği / Konu Başlıkları */
    private String contentTopics;

    /** JSON string — Eğitim Kazanımları */
    private String learningOutcomes;

    /** JSON string — Ön Koşullar */
    private String prerequisites;

    /** JSON string — Ölçme ve Değerlendirme (List&lt;AssessmentItem&gt;) */
    private String assessmentItems;
}
