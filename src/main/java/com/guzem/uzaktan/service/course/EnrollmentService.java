package com.guzem.uzaktan.service.course;

import com.guzem.uzaktan.dto.response.EnrollmentResponse;
import com.guzem.uzaktan.model.course.EnrollmentStatus;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface EnrollmentService {

    EnrollmentResponse enroll(Long userId, Long courseId);



    List<EnrollmentResponse> findByUser(Long userId);

    Page<EnrollmentResponse> findByCourse(Long courseId, int page, int size);

    Optional<EnrollmentResponse> findByUserAndCourse(Long userId, Long courseId);

    Page<EnrollmentResponse> findAllForAdmin(int page, int size);

    EnrollmentResponse activateEnrollment(Long enrollmentId);

    void deleteEnrollment(Long enrollmentId);

    void recalculateProgress(Long userId, Long courseId);

    long countByUserAndStatus(Long userId, EnrollmentStatus status);

    boolean isActiveEnrollment(Long userId, Long courseId);

    long countTotal();

    long countByStatus(EnrollmentStatus status);
}
