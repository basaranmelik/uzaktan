package com.guzem.uzaktan.service.impl.course;

import com.guzem.uzaktan.event.ReviewApprovedEvent;
import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.model.course.Course;
import com.guzem.uzaktan.model.course.CourseReview;
import com.guzem.uzaktan.model.course.CourseStatus;
import com.guzem.uzaktan.model.course.CourseType;
import com.guzem.uzaktan.repository.course.CourseRepository;
import com.guzem.uzaktan.repository.course.CourseReviewRepository;
import com.guzem.uzaktan.service.course.CourseAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseAdminServiceImpl implements CourseAdminService {

    private final CourseRepository courseRepository;
    private final CourseReviewRepository courseReviewRepository;

    @Override
    @Cacheable(value = "courseStats", key = "'status'")
    public Map<CourseStatus, Long> getStatusCounts() {
        Map<CourseStatus, Long> ordered = new LinkedHashMap<>();
        for (CourseStatus status : new CourseStatus[]{
                CourseStatus.PUBLISHED, CourseStatus.IN_PROGRESS,
                CourseStatus.DRAFT, CourseStatus.COMPLETED, CourseStatus.CANCELLED}) {
            ordered.put(status, courseRepository.countByStatus(status));
        }
        return ordered;
    }

    @Override
    @Cacheable(value = "courseStats", key = "'type'")
    public Map<CourseType, Long> getTypeCounts() {
        return Arrays.stream(CourseType.values())
                .collect(Collectors.toMap(
                        type -> type,
                        courseRepository::countByCourseType
                ));
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "publishedCourses", allEntries = true),
        @CacheEvict(value = "coursesByCategory", allEntries = true),
        @CacheEvict(value = "courseStats", allEntries = true),
        @CacheEvict(value = "featuredCourses", allEntries = true)
    })
    public void updateCourseStatuses() {
        List<Course> coursesToStart = courseRepository.findPublishedCoursesToStart();
        for (Course course : coursesToStart) {
            course.setStatus(CourseStatus.IN_PROGRESS);
        }
        courseRepository.saveAll(coursesToStart);

        List<Course> coursesToComplete = courseRepository.findInProgressCoursesToComplete();
        for (Course course : coursesToComplete) {
            course.setStatus(CourseStatus.COMPLETED);
        }
        courseRepository.saveAll(coursesToComplete);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "course", key = "#courseId"),
        @CacheEvict(value = "publishedCourses", allEntries = true),
        @CacheEvict(value = "coursesByCategory", allEntries = true),
        @CacheEvict(value = "courseStats", allEntries = true)
    })
    public void updateCourseRating(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Kurs", "id", courseId));
        List<CourseReview> approvedReviews = courseReviewRepository
                .findByCourseIdAndIsApprovedTrueOrderByCreatedAtDesc(courseId);

        int count = approvedReviews.size();
        course.setReviewCount(count);

        if (count > 0) {
            double totalRating = approvedReviews.stream().mapToInt(CourseReview::getRating).sum();
            course.setAverageRating(Math.round((totalRating / count) * 10.0) / 10.0);
        } else {
            course.setAverageRating(0.0);
        }

        courseRepository.save(course);
    }

    @Transactional
    @EventListener
    public void onReviewApproved(ReviewApprovedEvent event) {
        updateCourseRating(event.getCourseId());
    }
}
