package com.guzem.uzaktan.model.common;

public enum Role {
    ADMIN("Yönetici"),
    TEACHER("Öğretmen"),
    USER("Kullanıcı"),
    FIRM("Firma");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAuthority() {
        return "ROLE_" + name();
    }
}
