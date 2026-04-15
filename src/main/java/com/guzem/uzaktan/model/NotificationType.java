package com.guzem.uzaktan.model;

public enum NotificationType {
    ENROLLMENT_ACTIVE("Kayıt Onaylandı"),
    ASSIGNMENT_GRADED("Ödev Notlandırıldı"),
    CERTIFICATE_ISSUED("Sertifika Hazır"),
    REVIEW_APPROVED("Yorum Onaylandı"),
    ASSIGNMENT_DUE_SOON("Ödev Son Gün"),
    COURSE_ENDED("Kurs Tamamlandı");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
