package com.guzem.uzaktan.model;

public enum CourseCategory {
    SOFTWARE_DEVELOPMENT("Yazılım Geliştirme"),
    DATA_SCIENCE("Veri Bilimi ve Yapay Zeka"),
    BUSINESS("İşletme ve Finans"),
    DESIGN("Tasarım"),
    MARKETING("Pazarlama"),
    PERSONAL_DEVELOPMENT("Kişisel Gelişim"),
    PHOTOGRAPHY_AND_VIDEO("Fotoğrafçılık ve Video"),
    LANGUAGE_LEARNING("Dil Eğitimi"),
    OTHER("Diğer");

    private final String displayName;

    CourseCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}