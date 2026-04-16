package com.guzem.uzaktan.util;

public final class PhoneUtils {
    private PhoneUtils() {}

    /** Telefon gerçekten girilmiş mi (null, boş veya mask default değil mi)? */
    public static boolean isProvided(String phone) {
        return phone != null && !phone.trim().isEmpty() && !phone.equals("+90 ");
    }
}
