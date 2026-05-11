package com.guzem.uzaktan.repository.admin;

import com.guzem.uzaktan.model.admin.AssignmentSubmission;
import com.guzem.uzaktan.model.admin.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {

    @Query("SELECT s FROM AssignmentSubmission s JOIN FETCH s.user WHERE s.assignment.id = :assignmentId ORDER BY s.submittedAt DESC")
    List<AssignmentSubmission> findByAssignmentIdWithUser(@Param("assignmentId") Long assignmentId);

    Optional<AssignmentSubmission> findByAssignmentIdAndUserId(Long assignmentId, Long userId);

    @Query("SELECT s FROM AssignmentSubmission s JOIN FETCH s.assignment a JOIN FETCH a.course WHERE s.user.id = :userId ORDER BY s.submittedAt DESC")
    List<AssignmentSubmission> findByUserIdWithAssignment(@Param("userId") Long userId);

    boolean existsByAssignmentIdAndUserId(Long assignmentId, Long userId);

    @Query("SELECT COUNT(s) FROM AssignmentSubmission s JOIN s.assignment a WHERE a.course.id = :courseId AND s.status = :status")
    long countByCourseIdAndStatus(@Param("courseId") Long courseId, @Param("status") SubmissionStatus status);

    @Query("SELECT a.course.id, COUNT(s) FROM AssignmentSubmission s JOIN s.assignment a WHERE a.course.id IN :courseIds AND s.status = :status GROUP BY a.course.id")
    List<Object[]> countPendingByCourseIds(@Param("courseIds") List<Long> courseIds, @Param("status") SubmissionStatus status);

    @Query("SELECT s.assignment.id, COUNT(s) FROM AssignmentSubmission s WHERE s.assignment.id IN :assignmentIds AND s.status = :status GROUP BY s.assignment.id")
    List<Object[]> countPendingByAssignmentIds(@Param("assignmentIds") List<Long> assignmentIds, @Param("status") SubmissionStatus status);

    @Query("SELECT COUNT(s) FROM AssignmentSubmission s WHERE s.status = :status")
    long countByStatus(@Param("status") SubmissionStatus status);

    @Query("SELECT s.assignment.id, COUNT(s) FROM AssignmentSubmission s WHERE s.assignment.id IN :assignmentIds GROUP BY s.assignment.id")
    List<Object[]> countByAssignmentIds(@Param("assignmentIds") List<Long> assignmentIds);

    long countByAssignmentId(Long assignmentId);

    @Modifying
    @Query("DELETE FROM AssignmentSubmission s WHERE s.assignment.id IN :assignmentIds")
    void deleteByAssignmentIdIn(@Param("assignmentIds") java.util.Collection<Long> assignmentIds);
}
