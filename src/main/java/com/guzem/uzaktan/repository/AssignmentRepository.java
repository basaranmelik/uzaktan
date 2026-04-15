package com.guzem.uzaktan.repository;

import com.guzem.uzaktan.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByCourseIdOrderByDueDateAsc(Long courseId);

    @Query("SELECT a FROM Assignment a JOIN FETCH a.course WHERE a.dueDate >= :from AND a.dueDate < :to")
    List<Assignment> findDueBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT a FROM Assignment a JOIN FETCH a.course c
            JOIN Enrollment e ON e.course.id = c.id
            WHERE e.user.id = :userId
              AND e.status IN (com.guzem.uzaktan.model.EnrollmentStatus.ACTIVE,
                               com.guzem.uzaktan.model.EnrollmentStatus.COMPLETED)
            ORDER BY a.dueDate ASC
            """)
    List<Assignment> findByEnrolledUserId(@Param("userId") Long userId);

    long count();
}
