package com.guzem.uzaktan.service.course;

import com.guzem.uzaktan.dto.response.CourseResponse;

import java.util.List;

public interface CourseInstructorService {

    List<CourseResponse> findByInstructor(Long instructorId);

    long countTotalStudentsForInstructor(Long instructorId);

    long countActiveCoursesForInstructor(Long instructorId);

    long countTotalStudentsByInstructorName(String instructorName);

    long countActiveCoursesByInstructorName(String instructorName);
}
