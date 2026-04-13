package com.guzem.uzaktan.repository;

import com.guzem.uzaktan.model.Enrollment;
import com.guzem.uzaktan.model.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    List<Enrollment> findByUserIdAndStatus(Long userId, EnrollmentStatus status);

    Page<Enrollment> findByCourseId(Long courseId, Pageable pageable);

    @Query("SELECT e FROM Enrollment e JOIN FETCH e.course WHERE e.user.id = :userId ORDER BY e.enrollmentDate DESC")
    List<Enrollment> findByUserIdWithCourse(@Param("userId") Long userId);

    @Query(value = "SELECT e FROM Enrollment e JOIN FETCH e.user JOIN FETCH e.course ORDER BY e.enrollmentDate DESC",
           countQuery = "SELECT COUNT(e) FROM Enrollment e")
    Page<Enrollment> findAllWithDetails(Pageable pageable);

    long countByCourseId(Long courseId);

    long countByUserIdAndStatus(Long userId, EnrollmentStatus status);
}
