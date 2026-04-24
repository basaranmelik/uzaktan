package com.guzem.uzaktan.model.course;

public enum CorrectOption {
    A("A"),
    B("B"),
    C("C"),
    D("D"),
    E("E");

    private final String displayName;

    CorrectOption(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
