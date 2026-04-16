package com.guzem.uzaktan.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoProgressRequest {
    private int watchTimeDelta;
    private int currentPosition;
    private int duration;
    private boolean seeked;
}
