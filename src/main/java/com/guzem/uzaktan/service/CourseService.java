package com.guzem.uzaktan.service;

import com.guzem.uzaktan.dto.request.CourseCreateRequest;
import com.guzem.uzaktan.dto.request.CourseUpdateRequest;
import com.guzem.uzaktan.dto.response.CourseResponse;
import com.guzem.uzaktan.dto.response.CourseSummaryResponse;
import com.guzem.uzaktan.model.CourseCategory;
import com.guzem.uzaktan.model.CourseStatus;
import org.springframework.data.domain.Page;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface CourseService {

    Page<CourseSummaryResponse> findPublishedCourses(int page, int size);

    Page<CourseSummaryResponse> findByCategory(CourseCategory category, int page, int size);

    Page<CourseSummaryResponse> search(String keyword, int page, int size);

    Page<CourseSummaryResponse> findPublishedCoursesForUser(Long userId, int page, int size);

    CourseResponse findById(Long id);

    CourseResponse create(CourseCreateRequest request, MultipartFile image);

    CourseResponse update(Long id, CourseUpdateRequest request, MultipartFile image);

    void delete(Long id);

    void changeStatus(Long id, CourseStatus newStatus);

    Page<CourseResponse> findAllForAdmin(int page, int size);

    Map<CourseStatus, Long> getStatusCounts();

    Map<com.guzem.uzaktan.model.CourseType, Long> getTypeCounts();

    void updateCourseStatuses();

    List<CourseResponse> findByInstructor(Long instructorId);

    long countTotalStudentsForInstructor(Long instructorId);

    long countActiveCoursesForInstructor(Long instructorId);

    long countTotalStudentsByInstructorName(String instructorName);

    long countActiveCoursesByInstructorName(String instructorName);

    void updateCourseRating(Long courseId);
}
