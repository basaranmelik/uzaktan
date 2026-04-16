package com.guzem.uzaktan.dto.response;

import com.guzem.uzaktan.model.ZoomMeetingStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ZoomMeetingResponse {
    private Long id;
    private String topic;
    private String joinUrl;
    private String startUrl;
    private String password;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private ZoomMeetingStatus status;
    private String statusDisplayName;
    private Long courseId;
    private String courseTitle;
    private String recordingUrl;
    private boolean past;
    private boolean live;
    private LocalDateTime createdAt;
}
