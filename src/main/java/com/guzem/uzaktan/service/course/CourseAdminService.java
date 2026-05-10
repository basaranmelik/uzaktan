package com.guzem.uzaktan.service.course;

import com.guzem.uzaktan.model.course.CourseStatus;
import com.guzem.uzaktan.model.course.CourseType;

import java.util.Map;

public interface CourseAdminService {

    Map<CourseStatus, Long> getStatusCounts();

    Map<CourseType, Long> getTypeCounts();

    void updateCourseStatuses();

    void updateCourseRating(Long courseId);
}
