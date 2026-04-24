package com.guzem.uzaktan.model.instructor;

public enum ZoomMeetingStatus {
    SCHEDULED("Planlandı"),
    CANCELLED("İptal Edildi");

    private final String displayName;

    ZoomMeetingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
