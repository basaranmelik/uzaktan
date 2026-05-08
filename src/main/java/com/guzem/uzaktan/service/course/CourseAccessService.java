package com.guzem.uzaktan.service.course;

/**
 * Determines course access rights for different roles.
 */
public interface CourseAccessService {

    /**
     * Checks if a user has active access to a course's content (videos, etc.).
     * Admin always has access, teacher has access to their own courses,
     * students need ACTIVE or COMPLETED enrollment.
     */
    boolean hasActiveAccess(Long userId, Long courseId);

    /**
     * Checks if a user is a teacher or admin with management rights on a course.
     */
    boolean isTeacherOrAdminForCourse(Long userId, Long courseId);
}
