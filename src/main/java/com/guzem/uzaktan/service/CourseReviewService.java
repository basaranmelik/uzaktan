package com.guzem.uzaktan.service;

import com.guzem.uzaktan.dto.request.CourseReviewRequest;
import com.guzem.uzaktan.dto.response.CourseReviewResponse;

import java.util.List;

public interface CourseReviewService {
    
    CourseReviewResponse addReview(Long courseId, Long userId, CourseReviewRequest request);

    List<CourseReviewResponse> getApprovedReviewsByCourse(Long courseId);

    List<CourseReviewResponse> getPendingReviews();

    void approveReview(Long reviewId);

    void deleteReview(Long reviewId);
    
    boolean hasUserReviewed(Long courseId, Long userId);
}
