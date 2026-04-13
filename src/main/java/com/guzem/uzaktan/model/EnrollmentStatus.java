package com.guzem.uzaktan.model;

public enum EnrollmentStatus {
    PENDING_PAYMENT("Ödeme Bekleniyor"),
    ACTIVE("Aktif"),
    COMPLETED("Tamamlandı"),
    DROPPED("Bırakıldı");

    private final String displayName;

    EnrollmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}