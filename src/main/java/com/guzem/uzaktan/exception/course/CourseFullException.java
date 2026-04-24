package com.guzem.uzaktan.exception.course;

public class CourseFullException extends RuntimeException {

    public CourseFullException(Long courseId) {
        super(String.format("Kurs (id=%d) kontenjanı doldu.", courseId));
    }
}
