package com.guzem.uzaktan.service.impl;

import com.guzem.uzaktan.dto.response.EnrollmentResponse;
import com.guzem.uzaktan.exception.CourseFullException;
import com.guzem.uzaktan.exception.DuplicateEnrollmentException;
import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.exception.UnauthorizedActionException;
import com.guzem.uzaktan.mapper.EnrollmentMapper;
import com.guzem.uzaktan.model.Course;
import com.guzem.uzaktan.model.CourseType;
import com.guzem.uzaktan.model.Enrollment;
import com.guzem.uzaktan.model.EnrollmentStatus;
import com.guzem.uzaktan.model.User;
import com.guzem.uzaktan.repository.CourseRepository;
import com.guzem.uzaktan.repository.CourseVideoRepository;
import com.guzem.uzaktan.repository.EnrollmentRepository;
import com.guzem.uzaktan.repository.UserRepository;
import com.guzem.uzaktan.repository.VideoWatchRepository;

import com.guzem.uzaktan.service.EnrollmentService;
import com.guzem.uzaktan.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseVideoRepository courseVideoRepository;
    private final VideoWatchRepository videoWatchRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final NotificationService notificationService;

    @Override
    public EnrollmentResponse enroll(Long userId, Long courseId) {
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new DuplicateEnrollmentException(userId, courseId);
        }

        Course course = courseRepository.findByIdForUpdate(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Kurs", "id", courseId));

        if (course.getCourseType() != CourseType.ONLINE) {
            long activeEnrollments = enrollmentRepository.countByCourseId(courseId);
            if (course.getQuota() != null && activeEnrollments >= course.getQuota()) {
                throw new CourseFullException(courseId);
            }
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "id", userId));

        if (user.getRole() != com.guzem.uzaktan.model.Role.USER) {
            throw new UnauthorizedActionException(
                    "Sadece öğrenciler kurslara kayıt olabilir.");
        }

        // Ücretsiz kurslarda direkt aktif, ücretli kurslarda ödeme bekleniyor
        EnrollmentStatus initialStatus = (course.getPrice().compareTo(BigDecimal.ZERO) == 0)
                ? EnrollmentStatus.ACTIVE
                : EnrollmentStatus.PENDING_PAYMENT;

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .status(initialStatus)
                .progressPercentage(0)
                .build();

        EnrollmentResponse response = enrollmentMapper.toResponse(enrollmentRepository.save(enrollment));

        if (initialStatus == EnrollmentStatus.ACTIVE) {
            notificationService.create(user, com.guzem.uzaktan.model.NotificationType.ENROLLMENT_ACTIVE,
                    "Kursa Kayıt Olundu",
                    "\"" + course.getTitle() + "\" kursuna başarıyla kayıt oldunuz.",
                    "/panom");
        }

        return response;
    }



    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> findByUser(Long userId) {
        return enrollmentRepository.findByUserIdWithCourse(userId).stream()
                .map(enrollmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EnrollmentResponse> findByCourse(Long courseId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("enrollmentDate").descending());
        return enrollmentRepository.findByCourseId(courseId, pageable)
                .map(enrollmentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EnrollmentResponse> findByUserAndCourse(Long userId, Long courseId) {
        return enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .map(enrollmentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EnrollmentResponse> findAllForAdmin(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return enrollmentRepository.findAllWithDetails(pageable)
                .map(enrollmentMapper::toResponse);
    }

    @Override
    public EnrollmentResponse activateEnrollment(Long enrollmentId) {
        Enrollment enrollment = loadEnrollment(enrollmentId);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        EnrollmentResponse response = enrollmentMapper.toResponse(enrollmentRepository.save(enrollment));
        notificationService.create(enrollment.getUser(), com.guzem.uzaktan.model.NotificationType.ENROLLMENT_ACTIVE,
                "Kaydınız Onaylandı",
                "\"" + enrollment.getCourse().getTitle() + "\" kursuna kaydınız aktifleştirildi.",
                "/panom");
        return response;
    }

    @Override
    public void deleteEnrollment(Long enrollmentId) {
        Enrollment enrollment = loadEnrollment(enrollmentId);
        enrollmentRepository.delete(enrollment);
    }

    @Override
    public void recalculateProgress(Long userId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Kayıt", "userId+courseId", userId));

        long total = courseVideoRepository.countByCourseId(courseId);
        if (total == 0) {
            enrollment.setProgressPercentage(0);
            enrollmentRepository.save(enrollment);
            return;
        }

        long watched = videoWatchRepository.countWatchedByUserAndCourse(userId, courseId);
        int progress = (int) Math.min(100L, watched * 100L / total);
        enrollment.setProgressPercentage(progress);

        if (progress >= 100 && enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
        }

        enrollmentRepository.save(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByUserAndStatus(Long userId, EnrollmentStatus status) {
        return enrollmentRepository.countByUserIdAndStatus(userId, status);
    }

    private Enrollment loadEnrollment(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kayıt", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isActiveEnrollment(Long userId, Long courseId) {
        return enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .map(e -> e.getStatus() == EnrollmentStatus.ACTIVE || e.getStatus() == EnrollmentStatus.COMPLETED)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotal() {
        return enrollmentRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(EnrollmentStatus status) {
        return enrollmentRepository.countByStatus(status);
    }

}
