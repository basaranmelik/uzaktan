package com.guzem.uzaktan.exception;

public class CourseFullException extends RuntimeException {

    public CourseFullException(Long courseId) {
        super(String.format("Kurs (id=%d) kontenjanı doldu.", courseId));
    }
}
