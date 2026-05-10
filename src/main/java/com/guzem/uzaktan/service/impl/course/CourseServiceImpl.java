package com.guzem.uzaktan.service.impl.course;

import com.guzem.uzaktan.dto.request.CourseCreateRequest;
import com.guzem.uzaktan.dto.request.CourseUpdateRequest;
import com.guzem.uzaktan.dto.response.CourseResponse;
import com.guzem.uzaktan.dto.response.CourseSummaryResponse;
import com.guzem.uzaktan.model.course.CourseCategory;
import com.guzem.uzaktan.model.course.CourseStatus;
import com.guzem.uzaktan.model.course.CourseType;
import com.guzem.uzaktan.service.course.CourseAdminService;
import com.guzem.uzaktan.service.course.CourseInstructorService;
import com.guzem.uzaktan.service.course.CourseManagementService;
import com.guzem.uzaktan.service.course.CourseQueryService;
import com.guzem.uzaktan.service.course.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseQueryService courseQueryService;
    private final CourseManagementService courseManagementService;
    private final CourseInstructorService courseInstructorService;
    private final CourseAdminService courseAdminService;

    @Override public Page<CourseSummaryResponse> findPublishedCourses(String sort, int page, int size) { return courseQueryService.findPublishedCourses(sort, page, size); }
    @Override public Page<CourseSummaryResponse> findByCategory(CourseCategory category, String sort, int page, int size) { return courseQueryService.findByCategory(category, sort, page, size); }
    @Override public Page<CourseSummaryResponse> search(String keyword, String sort, int page, int size) { return courseQueryService.search(keyword, sort, page, size); }
    @Override public Page<CourseSummaryResponse> findPublishedCoursesForUser(Long userId, String sort, int page, int size) { return courseQueryService.findPublishedCoursesForUser(userId, sort, page, size); }
    @Override public CourseResponse findById(Long id) { return courseQueryService.findById(id); }
    @Override public Page<CourseResponse> findAllForAdmin(int page, int size) { return courseQueryService.findAllForAdmin(page, size); }
    @Override public CourseResponse create(CourseCreateRequest request, MultipartFile image, Long creatorId) { return courseManagementService.create(request, image, creatorId); }
    @Override public CourseResponse update(Long id, CourseUpdateRequest request, MultipartFile image) { return courseManagementService.update(id, request, image); }
    @Override public void delete(Long id) { courseManagementService.delete(id); }
    @Override public void changeStatus(Long id, CourseStatus newStatus) { courseManagementService.changeStatus(id, newStatus); }
    @Override public List<CourseResponse> findByInstructor(Long instructorId) { return courseInstructorService.findByInstructor(instructorId); }
    @Override public long countTotalStudentsForInstructor(Long instructorId) { return courseInstructorService.countTotalStudentsForInstructor(instructorId); }
    @Override public long countActiveCoursesForInstructor(Long instructorId) { return courseInstructorService.countActiveCoursesForInstructor(instructorId); }
    @Override public long countTotalStudentsByInstructorName(String instructorName) { return courseInstructorService.countTotalStudentsByInstructorName(instructorName); }
    @Override public long countActiveCoursesByInstructorName(String instructorName) { return courseInstructorService.countActiveCoursesByInstructorName(instructorName); }
    @Override public Map<CourseStatus, Long> getStatusCounts() { return courseAdminService.getStatusCounts(); }
    @Override public Map<CourseType, Long> getTypeCounts() { return courseAdminService.getTypeCounts(); }
    @Override public void updateCourseStatuses() { courseAdminService.updateCourseStatuses(); }
    @Override public void updateCourseRating(Long courseId) { courseAdminService.updateCourseRating(courseId); }
    @Override public List<CourseSummaryResponse> findFeaturedCourses() { return courseQueryService.findFeaturedCourses(); }
    @Override public boolean toggleFeatured(Long id) { return courseManagementService.toggleFeatured(id); }
}
