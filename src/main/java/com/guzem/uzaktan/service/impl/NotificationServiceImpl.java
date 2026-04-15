package com.guzem.uzaktan.service.impl;

import com.guzem.uzaktan.dto.response.NotificationResponse;
import com.guzem.uzaktan.model.Notification;
import com.guzem.uzaktan.model.NotificationType;
import com.guzem.uzaktan.model.User;
import com.guzem.uzaktan.repository.NotificationRepository;
import com.guzem.uzaktan.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public void create(User recipient, NotificationType type, String title, String message, String link) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .title(title)
                .message(message)
                .link(link)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndReadFalse(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getRecent(Long userId) {
        return notificationRepository.findTop5ByRecipientIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getAll(Long userId, int page, int size) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(
                userId, PageRequest.of(page, size))
                .map(this::toResponse);
    }

    @Override
    public void markAllRead(Long userId) {
        notificationRepository.markAllReadByRecipientId(userId);
    }

    @Override
    public void markRead(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getRecipient().getId().equals(userId) && !n.isRead()) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .typeDisplayName(n.getType().getDisplayName())
                .title(n.getTitle())
                .message(n.getMessage())
                .read(n.isRead())
                .link(n.getLink())
                .createdAt(n.getCreatedAt())
                .timeAgo(formatTimeAgo(n.getCreatedAt()))
                .build();
    }

    private String formatTimeAgo(LocalDateTime createdAt) {
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(createdAt, now);
        if (minutes < 1) return "Az önce";
        if (minutes < 60) return minutes + " dakika önce";
        long hours = ChronoUnit.HOURS.between(createdAt, now);
        if (hours < 24) return hours + " saat önce";
        long days = ChronoUnit.DAYS.between(createdAt, now);
        if (days < 7) return days + " gün önce";
        long weeks = days / 7;
        if (weeks < 5) return weeks + " hafta önce";
        long months = ChronoUnit.MONTHS.between(createdAt, now);
        return months + " ay önce";
    }
}
