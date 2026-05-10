package com.guzem.uzaktan.repository.course;

import com.guzem.uzaktan.model.course.CourseReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {
    
    List<CourseReview> findByCourseIdAndIsApprovedTrueOrderByCreatedAtDesc(Long courseId);

    List<CourseReview> findByIsApprovedFalseOrderByCreatedAtDesc();

    boolean existsByCourseIdAndUserId(Long courseId, Long userId);

    long countByIsApprovedFalse();

    @Modifying
    @Query("DELETE FROM CourseReview r WHERE r.course.id = :courseId")
    void deleteAllByCourseId(@Param("courseId") Long courseId);
}
