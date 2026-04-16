package com.guzem.uzaktan.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class ZoomMeetingUpdateRequest {

    @NotBlank(message = "Toplantı konusu boş olamaz.")
    private String topic;

    @NotNull(message = "Tarih ve saat zorunludur.")
    @Future(message = "Toplantı tarihi gelecekte olmalıdır.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime scheduledAt;

    @NotNull(message = "Süre zorunludur.")
    @Min(value = 15, message = "Süre en az 15 dakika olmalıdır.")
    @Max(value = 480, message = "Süre en fazla 480 dakika olabilir.")
    private Integer durationMinutes;
}
