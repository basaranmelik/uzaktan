package com.guzem.uzaktan.service.common;

public final class PasswordPolicy {

    public static final String REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%&*.,_\\-]).{10,}$";

    public static final String MESSAGE = "Şifre en az 10 karakter olmalı, büyük harf, küçük harf, rakam ve özel karakter (!@#$%&*.,_-) içermelidir.";

    public static final java.util.regex.Pattern PATTERN = java.util.regex.Pattern.compile(REGEX);

    private PasswordPolicy() {}
}
