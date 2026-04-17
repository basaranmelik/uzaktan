package com.guzem.uzaktan.model;

public enum NotificationType {
    ENROLLMENT_ACTIVE("Kayıt Onaylandı"),
    ASSIGNMENT_GRADED("Ödev Notlandırıldı"),
    CERTIFICATE_ISSUED("Sertifika Hazır"),
    REVIEW_APPROVED("Yorum Onaylandı"),
    REVIEW_PENDING("Onay Bekleyen Yorum"),
    ASSIGNMENT_DUE_SOON("Ödev Son Gün"),
    COURSE_ENDED("Kurs Tamamlandı"),
    MEETING_SCHEDULED("Yeni Canlı Ders"),
    MEETING_CANCELLED("Canlı Ders İptal"),
    MEETING_REMINDER("Canlı Ders Hatırlatma"),
    MEETING_STARTED("Ders Başladı");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
