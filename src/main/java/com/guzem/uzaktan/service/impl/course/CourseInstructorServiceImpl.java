package com.guzem.uzaktan.service.impl.course;

import com.guzem.uzaktan.dto.response.CourseResponse;
import com.guzem.uzaktan.mapper.course.CourseMapper;
import com.guzem.uzaktan.model.course.Course;
import com.guzem.uzaktan.model.course.CourseStatus;
import com.guzem.uzaktan.repository.course.CourseRepository;
import com.guzem.uzaktan.service.course.CourseInstructorService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseInstructorServiceImpl implements CourseInstructorService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    @Override
    @Cacheable(value = "instructorCourses", key = "#instructorId")
    public List<CourseResponse> findByInstructor(Long instructorId) {
        List<Course> courses = courseRepository.findByInstructorIdAndStatusNotWithCategory(instructorId, CourseStatus.CANCELLED);
        Map<Long, Long> enrollmentCounts = buildEnrollmentCountMap(
                courses.stream().map(Course::getId).toList());
        return courses.stream()
                .map(c -> courseMapper.toResponse(c, enrollmentCounts.getOrDefault(c.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    public long countTotalStudentsForInstructor(Long instructorId) {
        return findByInstructor(instructorId).stream()
                .mapToLong(CourseResponse::getEnrolledCount)
                .sum();
    }

    @Override
    public long countActiveCoursesForInstructor(Long instructorId) {
        return findByInstructor(instructorId).stream()
                .filter(c -> c.getStatus() == CourseStatus.PUBLISHED || c.getStatus() == CourseStatus.IN_PROGRESS)
                .count();
    }

    @Override
    public long countTotalStudentsByInstructorName(String instructorName) {
        return courseRepository.countDistinctStudentsByInstructorName(instructorName);
    }

    @Override
    public long countActiveCoursesByInstructorName(String instructorName) {
        return courseRepository.countActiveCoursesByInstructorName(instructorName);
    }

    private Map<Long, Long> buildEnrollmentCountMap(List<Long> courseIds) {
        if (courseIds.isEmpty()) return Map.of();
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : courseRepository.countActiveEnrollmentsByCourseIds(courseIds)) {
            map.put((Long) row[0], (Long) row[1]);
        }
        return map;
    }
}
