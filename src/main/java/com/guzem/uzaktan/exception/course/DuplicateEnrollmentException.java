package com.guzem.uzaktan.exception.course;

public class DuplicateEnrollmentException extends RuntimeException {

    public DuplicateEnrollmentException(Long userId, Long courseId) {
        super(String.format("Kullanıcı (id=%d) zaten bu kursa kayıtlı (courseId=%d).", userId, courseId));
    }
}
