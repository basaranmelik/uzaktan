package com.guzem.uzaktan.service;

import com.guzem.uzaktan.dto.response.NotificationResponse;
import com.guzem.uzaktan.model.NotificationType;
import com.guzem.uzaktan.model.User;
import org.springframework.data.domain.Page;

import java.util.List;

public interface NotificationService {

    void create(User recipient, NotificationType type, String title, String message, String link);

    long getUnreadCount(Long userId);

    List<NotificationResponse> getRecent(Long userId);

    Page<NotificationResponse> getAll(Long userId, int page, int size);

    void markAllRead(Long userId);

    void markRead(Long notificationId, Long userId);
}
