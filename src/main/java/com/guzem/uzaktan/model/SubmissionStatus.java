package com.guzem.uzaktan.model;

public enum SubmissionStatus {
    SUBMITTED("Teslim Edildi"),
    GRADED("Notlandırıldı");

    private final String displayName;

    SubmissionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
