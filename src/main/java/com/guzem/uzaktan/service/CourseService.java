package com.guzem.uzaktan.service;

import com.guzem.uzaktan.dto.request.CourseCreateRequest;
import com.guzem.uzaktan.dto.request.CourseUpdateRequest;
import com.guzem.uzaktan.dto.response.CourseResponse;
import com.guzem.uzaktan.dto.response.CourseSummaryResponse;
import com.guzem.uzaktan.model.CourseCategory;
import com.guzem.uzaktan.model.CourseStatus;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface CourseService {

    Page<CourseSummaryResponse> findPublishedCourses(int page, int size);

    Page<CourseSummaryResponse> findByCategory(CourseCategory category, int page, int size);

    Page<CourseSummaryResponse> search(String keyword, int page, int size);

    Page<CourseSummaryResponse> findPublishedCoursesForUser(Long userId, int page, int size);

    CourseResponse findById(Long id);

    CourseResponse create(CourseCreateRequest request);

    CourseResponse update(Long id, CourseUpdateRequest request);

    void delete(Long id);

    void changeStatus(Long id, CourseStatus newStatus);

    Page<CourseResponse> findAllForAdmin(int page, int size);

    Map<CourseStatus, Long> getStatusCounts();

    List<CourseResponse> findByInstructor(Long instructorId);

    long countTotalStudentsForInstructor(Long instructorId);

    long countActiveCoursesForInstructor(Long instructorId);

    void updateCourseRating(Long courseId);
}
