package com.guzem.uzaktan.dto.response;

import com.guzem.uzaktan.model.user.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private NotificationType type;
    private String typeDisplayName;
    private String title;
    private String message;
    private boolean read;
    private String link;
    private LocalDateTime createdAt;
    private String timeAgo;
}
