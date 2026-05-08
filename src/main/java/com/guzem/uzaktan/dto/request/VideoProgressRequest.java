package com.guzem.uzaktan.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoProgressRequest {
    @Min(0) private int watchTimeDelta;
    @Min(0) private int currentPosition;
    @Min(1) @Max(86400) private int duration;
    private boolean seeked;
}
