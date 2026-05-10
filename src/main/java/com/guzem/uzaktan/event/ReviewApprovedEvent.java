package com.guzem.uzaktan.event;

public class ReviewApprovedEvent {
    private final Long courseId;

    public ReviewApprovedEvent(Long courseId) {
        this.courseId = courseId;
    }

    public Long getCourseId() {
        return courseId;
    }
}
