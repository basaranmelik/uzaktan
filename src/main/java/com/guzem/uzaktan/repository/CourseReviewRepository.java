package com.guzem.uzaktan.repository;

import com.guzem.uzaktan.model.CourseReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {
    
    List<CourseReview> findByCourseIdAndIsApprovedTrueOrderByCreatedAtDesc(Long courseId);

    List<CourseReview> findByIsApprovedFalseOrderByCreatedAtDesc();

    boolean existsByCourseIdAndUserId(Long courseId, Long userId);
}
