package com.guzem.uzaktan.repository.course;

import com.guzem.uzaktan.model.course.Course;
import com.guzem.uzaktan.model.course.CourseCategory;
import com.guzem.uzaktan.model.course.CourseStatus;
import com.guzem.uzaktan.model.course.CourseType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Page<Course> findByStatus(CourseStatus status, Pageable pageable);

    Page<Course> findByStatusAndCategory(CourseStatus status, CourseCategory category, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.status = :status AND " +
           "(LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Course> searchByKeyword(@Param("keyword") String keyword,
                                  @Param("status") CourseStatus status,
                                  Pageable pageable);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId " +
           "AND e.status IN (PENDING_PAYMENT, ACTIVE)")
    long countActiveEnrollments(@Param("courseId") Long courseId);

    @Query("SELECT e.course.id, COUNT(e) FROM Enrollment e WHERE e.course.id IN :courseIds " +
           "AND e.status IN (PENDING_PAYMENT, ACTIVE) GROUP BY e.course.id")
    List<Object[]> countActiveEnrollmentsByCourseIds(@Param("courseIds") List<Long> courseIds);

    long countByStatus(CourseStatus status);

    long countByCourseType(CourseType courseType);

    @Query("SELECT c FROM Course c WHERE c.status = 'PUBLISHED' AND c.startDate <= CURRENT_DATE")
    List<Course> findPublishedCoursesToStart();

    @Query("SELECT c FROM Course c WHERE c.status = 'IN_PROGRESS' AND c.endDate < CURRENT_DATE")
    List<Course> findInProgressCoursesToComplete();

    List<Course> findByInstructorIdAndStatusNot(Long instructorId, CourseStatus status);

    @Query("SELECT c FROM Course c JOIN c.instructors i WHERE i.id = :instructorId AND c.status != :status")
    List<Course> findByInstructorEntityIdAndStatusNot(@Param("instructorId") Long instructorId, @Param("status") CourseStatus status);

    @Query("SELECT c FROM Course c JOIN c.instructors i WHERE i.id = :instructorId")
    List<Course> findByInstructorEntityId(@Param("instructorId") Long instructorId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Course c WHERE c.id = :id")
    Optional<Course> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT COUNT(c) FROM Course c WHERE c.instructorName = :name AND c.status IN (PUBLISHED, IN_PROGRESS)")
    long countActiveCoursesByInstructorName(@Param("name") String name);

    @Query("SELECT COUNT(DISTINCT e.user.id) FROM Enrollment e WHERE e.course.instructorName = :name")
    long countDistinctStudentsByInstructorName(@Param("name") String name);
}
