package com.guzem.uzaktan.model.course;

public enum CourseType {
    ONLINE("Online"),
    HYBRID("Hibrit"),
    FACE_TO_FACE("Yüzyüze");

    private final String displayName;

    CourseType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
