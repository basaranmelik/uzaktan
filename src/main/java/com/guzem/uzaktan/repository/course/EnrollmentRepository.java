package com.guzem.uzaktan.repository.course;

import com.guzem.uzaktan.model.common.User;
import com.guzem.uzaktan.model.course.Enrollment;
import com.guzem.uzaktan.model.course.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
    
    boolean existsByUserIdAndCourseIdAndStatus(Long userId, Long courseId, EnrollmentStatus status);

    Page<Enrollment> findByCourseId(Long courseId, Pageable pageable);

    @Query("SELECT e FROM Enrollment e JOIN FETCH e.course WHERE e.user.id = :userId ORDER BY e.enrollmentDate DESC")
    List<Enrollment> findByUserIdWithCourse(@Param("userId") Long userId);

    @Query(value = "SELECT e FROM Enrollment e JOIN FETCH e.user JOIN FETCH e.course ORDER BY e.enrollmentDate DESC",
           countQuery = "SELECT COUNT(e) FROM Enrollment e")
    Page<Enrollment> findAllWithDetails(Pageable pageable);

    long countByCourseId(Long courseId);

    long countByUserIdAndStatus(Long userId, EnrollmentStatus status);

    long countByStatus(EnrollmentStatus status);

    @Query("""
            SELECT e FROM Enrollment e JOIN FETCH e.user
            WHERE e.course.endDate = :endDate
              AND e.status = ACTIVE
            """)
    List<Enrollment> findActiveEnrollmentsForCoursesEndingOn(@Param("endDate") LocalDate endDate);

    @Query("SELECT e FROM Enrollment e JOIN FETCH e.user WHERE e.course.id = :courseId AND e.status = ACTIVE")
    List<Enrollment> findActiveEnrollmentsForCourse(@Param("courseId") Long courseId);

    @Query("""
            SELECT e.user FROM Enrollment e
            WHERE e.course.id = :courseId
              AND e.status = ACTIVE
              AND NOT EXISTS (
                SELECT s FROM AssignmentSubmission s
                WHERE s.assignment.id = :assignmentId AND s.user.id = e.user.id
              )
            """)
    List<User> findActiveUsersWithoutSubmission(
            @Param("courseId") Long courseId,
            @Param("assignmentId") Long assignmentId);
}
