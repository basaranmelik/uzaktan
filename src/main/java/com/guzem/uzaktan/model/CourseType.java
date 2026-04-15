package com.guzem.uzaktan.model;

public enum CourseType {
    ONLINE("Online Eğitim"),
    FACE_TO_FACE("Yüzyüze Eğitim"),
    REMOTE_FORMAL("Uzaktan Örgün Eğitim");

    private final String displayName;

    CourseType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
