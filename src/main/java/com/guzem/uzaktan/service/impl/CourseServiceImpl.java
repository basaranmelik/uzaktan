package com.guzem.uzaktan.service.impl;

import com.guzem.uzaktan.dto.request.CourseCreateRequest;
import com.guzem.uzaktan.dto.request.CourseUpdateRequest;
import com.guzem.uzaktan.dto.response.CourseResponse;
import com.guzem.uzaktan.dto.response.CourseSummaryResponse;
import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.mapper.CourseMapper;
import com.guzem.uzaktan.model.Course;
import com.guzem.uzaktan.model.CourseCategory;
import com.guzem.uzaktan.model.CourseStatus;
import com.guzem.uzaktan.model.User;
import com.guzem.uzaktan.model.CourseReview;
import com.guzem.uzaktan.repository.CourseRepository;
import com.guzem.uzaktan.repository.CourseReviewRepository;
import com.guzem.uzaktan.repository.EnrollmentRepository;
import com.guzem.uzaktan.repository.UserRepository;
import com.guzem.uzaktan.service.CourseService;
import com.guzem.uzaktan.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseServiceImpl implements CourseService {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5 MB

    private final CourseRepository courseRepository;
    private final CourseReviewRepository courseReviewRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final CourseMapper courseMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<CourseSummaryResponse> findPublishedCourses(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return courseRepository.findByStatus(CourseStatus.PUBLISHED, pageable)
                .map(c -> courseMapper.toSummaryResponse(c, false));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseSummaryResponse> findByCategory(CourseCategory category, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return courseRepository.findByStatusAndCategory(CourseStatus.PUBLISHED, category, pageable)
                .map(c -> courseMapper.toSummaryResponse(c, false));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseSummaryResponse> search(String keyword, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return courseRepository.searchByKeyword(keyword, CourseStatus.PUBLISHED, pageable)
                .map(c -> courseMapper.toSummaryResponse(c, false));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseSummaryResponse> findPublishedCoursesForUser(Long userId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Course> courses = courseRepository.findByStatus(CourseStatus.PUBLISHED, pageable);

        // Tek sorguda kullanıcının kayıtlı olduğu kurs ID'lerini çek
        Set<Long> enrolledCourseIds = enrollmentRepository.findByUserIdWithCourse(userId).stream()
                .map(e -> e.getCourse().getId())
                .collect(Collectors.toSet());

        return courses.map(c -> courseMapper.toSummaryResponse(c, enrolledCourseIds.contains(c.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse findById(Long id) {
        Course course = loadCourse(id);
        long enrolledCount = courseRepository.countActiveEnrollments(id);
        return courseMapper.toResponse(course, enrolledCount);
    }

    @Override
    public CourseResponse create(CourseCreateRequest request, MultipartFile image) {
        Course course = courseMapper.toEntity(request);
        if (request.getInstructorId() != null) {
            User instructor = userRepository.findById(request.getInstructorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "id", request.getInstructorId()));
            course.setInstructor(instructor);
        }
        Course saved = courseRepository.save(course);
        handleCourseImage(saved, image);
        saved = courseRepository.save(saved);
        return courseMapper.toResponse(saved, 0);
    }

    @Override
    public CourseResponse update(Long id, CourseUpdateRequest request, MultipartFile image) {
        Course course = loadCourse(id);
        courseMapper.updateEntity(course, request);
        if (request.getInstructorId() != null) {
            User instructor = userRepository.findById(request.getInstructorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "id", request.getInstructorId()));
            course.setInstructor(instructor);
        }
        handleCourseImage(course, image);
        Course saved = courseRepository.save(course);
        long enrolledCount = courseRepository.countActiveEnrollments(id);
        return courseMapper.toResponse(saved, enrolledCount);
    }

    private void handleCourseImage(Course course, MultipartFile image) {
        if (image == null || image.isEmpty()) return;
        if (image.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("Fotoğraf boyutu en fazla 5 MB olabilir.");
        }
        if (course.getImagePath() != null) {
            fileStorageService.delete(course.getImagePath());
        }
        try {
            String path = fileStorageService.storeCourseImage(image, course.getId());
            course.setImagePath(path);
        } catch (IOException e) {
            throw new IllegalStateException("Fotoğraf yüklenirken hata oluştu.", e);
        }
    }

    @Override
    public void delete(Long id) {
        Course course = loadCourse(id);
        courseRepository.delete(course);
    }

    @Override
    public void changeStatus(Long id, CourseStatus newStatus) {
        Course course = loadCourse(id);
        course.setStatus(newStatus);
        courseRepository.save(course);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponse> findAllForAdmin(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Course> coursePage = courseRepository.findAll(pageable);
        Map<Long, Long> enrollmentCounts = buildEnrollmentCountMap(
                coursePage.getContent().stream().map(Course::getId).toList());
        return coursePage.map(c -> courseMapper.toResponse(c, enrollmentCounts.getOrDefault(c.getId(), 0L)));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<CourseStatus, Long> getStatusCounts() {
        return Arrays.stream(CourseStatus.values())
                .collect(Collectors.toMap(
                        status -> status,
                        courseRepository::countByStatus
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> findByInstructor(Long instructorId) {
        List<Course> courses = courseRepository.findByInstructorIdAndStatusNot(instructorId, CourseStatus.CANCELLED);
        Map<Long, Long> enrollmentCounts = buildEnrollmentCountMap(
                courses.stream().map(Course::getId).toList());
        return courses.stream()
                .map(c -> courseMapper.toResponse(c, enrollmentCounts.getOrDefault(c.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotalStudentsForInstructor(Long instructorId) {
        return findByInstructor(instructorId).stream()
                .mapToLong(CourseResponse::getEnrolledCount)
                .sum();
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveCoursesForInstructor(Long instructorId) {
        return findByInstructor(instructorId).stream()
                .filter(c -> c.getStatus() == CourseStatus.PUBLISHED || c.getStatus() == CourseStatus.IN_PROGRESS)
                .count();
    }

    @Override
    @Transactional
    public void updateCourseRating(Long courseId) {
        Course course = loadCourse(courseId);
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

    private Map<Long, Long> buildEnrollmentCountMap(List<Long> courseIds) {
        if (courseIds.isEmpty()) return Map.of();
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : courseRepository.countActiveEnrollmentsByCourseIds(courseIds)) {
            map.put((Long) row[0], (Long) row[1]);
        }
        return map;
    }

    private Course loadCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kurs", "id", id));
    }
}
