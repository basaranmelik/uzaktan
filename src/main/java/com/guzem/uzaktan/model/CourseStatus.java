package com.guzem.uzaktan.model;

public enum CourseStatus {
    DRAFT("Taslak"),
    PUBLISHED("Yayında"),
    IN_PROGRESS("Devam Ediyor"),
    COMPLETED("Tamamlandı"),
    CANCELLED("İptal Edildi");

    private final String displayName;

    CourseStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
