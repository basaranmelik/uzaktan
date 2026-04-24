package com.guzem.uzaktan.model.course;

public enum CourseLevel {
    BEGINNER("Başlangıç Seviye"),
    INTERMEDIATE("Orta Seviye"),
    ADVANCED("İleri Seviye");

    private final String displayName;

    CourseLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
