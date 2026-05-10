package com.guzem.uzaktan.service.impl.course;

import com.guzem.uzaktan.dto.response.CourseResponse;
import com.guzem.uzaktan.dto.response.CourseSummaryResponse;
import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.mapper.course.CourseMapper;
import com.guzem.uzaktan.model.course.Course;
import com.guzem.uzaktan.model.course.CourseCategory;
import com.guzem.uzaktan.model.course.CourseStatus;
import com.guzem.uzaktan.repository.course.CourseRepository;
import com.guzem.uzaktan.repository.course.EnrollmentRepository;
import com.guzem.uzaktan.service.course.CourseQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseQueryServiceImpl implements CourseQueryService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseMapper courseMapper;

    @Override
    @Cacheable(value = "publishedCourses", key = "{#sort}-{#page}-{#size}",
               condition = "#sort == 'default' && #page == 0")
    public Page<CourseSummaryResponse> findPublishedCourses(String sort, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, resolveSort(sort));
        Page<Course> coursePage = courseRepository.findByStatus(CourseStatus.PUBLISHED, pageable);
        Map<Long, Long> counts = buildEnrollmentCountMap(coursePage.getContent().stream().map(Course::getId).toList());
        return coursePage.map(c -> courseMapper.toSummaryResponse(c, false, counts.getOrDefault(c.getId(), 0L)));
    }

    @Override
    @Cacheable(value = "coursesByCategory", key = "{#category.displayName}-{#sort}-{#page}-{#size}",
               condition = "#sort == 'default' && #page == 0")
    public Page<CourseSummaryResponse> findByCategory(CourseCategory category, String sort, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, resolveSort(sort));
        Page<Course> coursePage = courseRepository.findByStatusAndCategory(CourseStatus.PUBLISHED, category, pageable);
        Map<Long, Long> counts = buildEnrollmentCountMap(coursePage.getContent().stream().map(Course::getId).toList());
        return coursePage.map(c -> courseMapper.toSummaryResponse(c, false, counts.getOrDefault(c.getId(), 0L)));
    }

    @Override
    public Page<CourseSummaryResponse> search(String keyword, String sort, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, resolveSort(sort));
        Page<Course> coursePage = courseRepository.searchByKeyword(keyword, CourseStatus.PUBLISHED, pageable);
        Map<Long, Long> counts = buildEnrollmentCountMap(coursePage.getContent().stream().map(Course::getId).toList());
        return coursePage.map(c -> courseMapper.toSummaryResponse(c, false, counts.getOrDefault(c.getId(), 0L)));
    }

    @Override
    public Page<CourseSummaryResponse> findPublishedCoursesForUser(Long userId, String sort, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, resolveSort(sort));
        Page<Course> courses = courseRepository.findByStatus(CourseStatus.PUBLISHED, pageable);
        Set<Long> enrolledCourseIds = enrollmentRepository.findByUserIdWithCourse(userId).stream()
                .map(e -> e.getCourse().getId())
                .collect(Collectors.toSet());
        Map<Long, Long> counts = buildEnrollmentCountMap(courses.getContent().stream().map(Course::getId).toList());
        return courses.map(c -> courseMapper.toSummaryResponse(c, enrolledCourseIds.contains(c.getId()), counts.getOrDefault(c.getId(), 0L)));
    }

    @Override
    @Cacheable(value = "course", key = "#id")
    public CourseResponse findById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kurs", "id", id));
        long enrolledCount = courseRepository.countActiveEnrollments(id);
        return courseMapper.toResponse(course, enrolledCount);
    }

    @Override
    @Cacheable(value = "featuredCourses")
    public List<CourseSummaryResponse> findFeaturedCourses() {
        List<Course> courses = courseRepository.findByFeaturedTrueAndStatusOrderByCreatedAtDesc(CourseStatus.PUBLISHED);
        Map<Long, Long> counts = buildEnrollmentCountMap(courses.stream().map(Course::getId).toList());
        List<CourseSummaryResponse> results = new ArrayList<>();
        for (Course c : courses) {
            results.add(courseMapper.toSummaryResponse(c, false, counts.getOrDefault(c.getId(), 0L)));
        }
        return results;
    }

    @Override
    public Page<CourseResponse> findAllForAdmin(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Course> coursePage = courseRepository.findAll(pageable);
        Map<Long, Long> enrollmentCounts = buildEnrollmentCountMap(
                coursePage.getContent().stream().map(Course::getId).toList());
        return coursePage.map(c -> courseMapper.toResponse(c, enrollmentCounts.getOrDefault(c.getId(), 0L)));
    }

    private Map<Long, Long> buildEnrollmentCountMap(List<Long> courseIds) {
        if (courseIds.isEmpty()) return Map.of();
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : courseRepository.countActiveEnrollmentsByCourseIds(courseIds)) {
            map.put((Long) row[0], (Long) row[1]);
        }
        return map;
    }

    private Sort resolveSort(String sort) {
        return switch (sort) {
            case "az" -> Sort.by("title").ascending();
            case "za" -> Sort.by("title").descending();
            case "price-asc" -> Sort.by("price").ascending();
            case "price-desc" -> Sort.by("price").descending();
            default -> Sort.by(Sort.Order.desc("featured"), Sort.Order.desc("createdAt"));
        };
    }
}
