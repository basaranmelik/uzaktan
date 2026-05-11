package com.guzem.uzaktan.service.impl.course;

import com.guzem.uzaktan.dto.request.CourseCreateRequest;
import com.guzem.uzaktan.dto.request.CourseUpdateRequest;
import com.guzem.uzaktan.dto.response.CourseResponse;
import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.mapper.course.CourseMapper;
import com.guzem.uzaktan.model.common.User;
import com.guzem.uzaktan.model.course.Course;
import com.guzem.uzaktan.model.course.CourseStatus;
import com.guzem.uzaktan.model.course.CourseType;
import com.guzem.uzaktan.repository.admin.AssignmentRepository;
import com.guzem.uzaktan.repository.admin.AssignmentSubmissionRepository;
import com.guzem.uzaktan.repository.course.*;
import com.guzem.uzaktan.repository.user.CartItemRepository;
import com.guzem.uzaktan.repository.user.UserRepository;
import com.guzem.uzaktan.service.course.CourseManagementService;
import com.guzem.uzaktan.service.common.FileStorageService;
import com.guzem.uzaktan.service.instructor.ZoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CourseManagementServiceImpl implements CourseManagementService {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseMapper courseMapper;
    private final FileStorageService fileStorageService;
    private final ZoomService zoomService;
    private final CartItemRepository cartItemRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final CourseReviewRepository courseReviewRepository;
    private final CertificateRepository certificateRepository;
    private final QuestionRepository questionRepository;
    private final CourseDocumentRepository courseDocumentRepository;
    private final CourseVideoRepository courseVideoRepository;
    private final VideoWatchRepository videoWatchRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    @CacheEvict(value = {"publishedCourses", "coursesByCategory", "courseStats", "featuredCourses", "instructorCourses"}, allEntries = true)
    public CourseResponse create(CourseCreateRequest request, MultipartFile image, Long creatorId) {
        validateCourseTypeFields(request.getCourseType(), request.getQuota(),
                request.getStartDate(), request.getEndDate(), request.getLocation());
        normalizePrice(request);
        Course course = courseMapper.toEntity(request);

        Long resolvedInstructorId = request.getInstructorId() != null ? request.getInstructorId() : creatorId;
        if (resolvedInstructorId != null) {
            User instructor = userRepository.findById(resolvedInstructorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "id", resolvedInstructorId));
            course.setInstructor(instructor);
            course.setInstructors(new ArrayList<>(List.of(instructor)));
            course.setInstructorName(instructor.getFirstName() + " " + instructor.getLastName());
        }

        Course saved = courseRepository.save(course);
        handleCourseImage(saved, image);
        saved = courseRepository.save(saved);
        zoomService.generateScheduledMeetings(saved.getId());
        return courseMapper.toResponse(saved, 0);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "course", key = "#id"),
        @CacheEvict(value = "publishedCourses", allEntries = true),
        @CacheEvict(value = "coursesByCategory", allEntries = true),
        @CacheEvict(value = "courseStats", allEntries = true),
        @CacheEvict(value = "instructorCourses", allEntries = true)
    })
    public CourseResponse update(Long id, CourseUpdateRequest request, MultipartFile image) {
        Course course = loadCourse(id);
        CourseType effectiveType = request.getCourseType() != null ? request.getCourseType() : course.getCourseType();
        Integer effectiveQuota = request.getQuota() != null ? request.getQuota() : course.getQuota();
        validateCourseTypeFields(effectiveType, effectiveQuota,
                request.getStartDate() != null ? request.getStartDate() : course.getStartDate(),
                request.getEndDate() != null ? request.getEndDate() : course.getEndDate(),
                request.getLocation() != null ? request.getLocation() : course.getLocation());
        normalizePrice(request);
        courseMapper.updateEntity(course, request);

        if (request.getInstructorId() != null) {
            User instructor = userRepository.findById(request.getInstructorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "id", request.getInstructorId()));
            course.setInstructor(instructor);
            course.setInstructors(new ArrayList<>(List.of(instructor)));
            course.setInstructorName(instructor.getFirstName() + " " + instructor.getLastName());
        }

        handleCourseImage(course, image);
        Course saved = courseRepository.save(course);
        long enrolledCount = courseRepository.countActiveEnrollments(id);
        return courseMapper.toResponse(saved, enrolledCount);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "course", key = "#id"),
        @CacheEvict(value = "publishedCourses", allEntries = true),
        @CacheEvict(value = "coursesByCategory", allEntries = true),
        @CacheEvict(value = "courseStats", allEntries = true),
        @CacheEvict(value = "instructorCourses", allEntries = true)
    })
    public void delete(Long id) {
        Course course = loadCourse(id);
        if (course.getImagePath() != null) {
            try { fileStorageService.delete(course.getImagePath()); } catch (Exception e) { log.error("Kurs görseli silinirken hata oluştu: {}", e.getMessage(), e); }
        }
        deleteCourseAssociations(id);
        courseRepository.delete(course);
    }

    private void deleteCourseAssociations(Long id) {
        cartItemRepository.deleteAllByCourseId(id);
        quizAttemptRepository.deleteAllByCourseId(id);
        courseReviewRepository.deleteAllByCourseId(id);
        certificateRepository.findByCourseId(id).stream()
                .filter(c -> c.getFileUrl() != null)
                .forEach(c -> {
                    try { fileStorageService.delete(c.getFileUrl()); } catch (Exception e) { log.warn("Sertifika PDF silinemedi: {}", c.getFileUrl()); }
                });
        certificateRepository.deleteAllByCourseId(id);
        questionRepository.deleteAllByCourseId(id);
        courseDocumentRepository.deleteAllByCourseId(id);

        List<Long> videoIds = courseVideoRepository.findIdsByCourseId(id);
        if (!videoIds.isEmpty()) {
            videoWatchRepository.deleteByVideoIdIn(videoIds);
        }
        courseVideoRepository.deleteAllByCourseId(id);

        List<Long> assignmentIds = assignmentRepository.findIdsByCourseId(id);
        if (!assignmentIds.isEmpty()) {
            assignmentSubmissionRepository.deleteByAssignmentIdIn(assignmentIds);
        }
        assignmentRepository.deleteAllByCourseId(id);

        enrollmentRepository.deleteAllByCourseId(id);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "course", key = "#id"),
        @CacheEvict(value = "publishedCourses", allEntries = true),
        @CacheEvict(value = "coursesByCategory", allEntries = true),
        @CacheEvict(value = "courseStats", allEntries = true),
        @CacheEvict(value = "instructorCourses", allEntries = true)
    })
    public void changeStatus(Long id, CourseStatus newStatus) {
        Course course = loadCourse(id);
        course.setStatus(newStatus);
        courseRepository.save(course);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "course", key = "#id"),
        @CacheEvict(value = "publishedCourses", allEntries = true),
        @CacheEvict(value = "coursesByCategory", allEntries = true),
        @CacheEvict(value = "courseStats", allEntries = true),
        @CacheEvict(value = "featuredCourses", allEntries = true),
        @CacheEvict(value = "instructorCourses", allEntries = true)
    })
    public boolean toggleFeatured(Long id) {
        Course course = loadCourse(id);
        course.setFeatured(!course.isFeatured());
        courseRepository.save(course);
        return course.isFeatured();
    }

    private Course loadCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kurs", "id", id));
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
            String path = fileStorageService.storeCourseImage(image, course.getId(), course.getTitle());
            course.setImagePath(path);
        } catch (IOException e) {
            throw new IllegalStateException("Fotoğraf yüklenirken hata oluştu.", e);
        }
    }

    private void validateCourseTypeFields(CourseType type, Integer quota,
            java.time.LocalDate startDate, java.time.LocalDate endDate, String location) {
        if (type == null || type == CourseType.ONLINE) return;
        if (startDate == null) throw new IllegalArgumentException("Başlangıç tarihi zorunludur.");
        if (endDate == null) throw new IllegalArgumentException("Bitiş tarihi zorunludur.");
        if (startDate.isAfter(endDate))
            throw new IllegalArgumentException("Başlangıç tarihi, bitiş tarihinden sonra olamaz.");
        if (quota == null || quota < 1) throw new IllegalArgumentException("Kontenjan zorunludur.");
        if (type == CourseType.FACE_TO_FACE && (location == null || location.isBlank()))
            throw new IllegalArgumentException("Yüzyüze eğitimler için konum bilgisi zorunludur.");
    }

    private void normalizePrice(CourseCreateRequest request) {
        if (request.getPrice() != null) {
            request.setPrice(request.getPrice().setScale(2, RoundingMode.HALF_UP));
        }
    }

    private void normalizePrice(CourseUpdateRequest request) {
        if (request.getPrice() != null) {
            request.setPrice(request.getPrice().setScale(2, RoundingMode.HALF_UP));
        }
    }
}
