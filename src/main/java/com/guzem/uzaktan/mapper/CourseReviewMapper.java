package com.guzem.uzaktan.mapper;

import com.guzem.uzaktan.dto.response.CourseReviewResponse;
import com.guzem.uzaktan.model.CourseReview;
import org.springframework.stereotype.Component;

@Component
public class CourseReviewMapper {

    public CourseReviewResponse toResponse(CourseReview review) {
        return CourseReviewResponse.builder()
                .id(review.getId())
                .courseId(review.getCourse().getId())
                .userId(review.getUser().getId())
                .userFullName(review.getUser().getFirstName() + " " + review.getUser().getLastName())
                .rating(review.getRating())
                .comment(review.getComment())
                .isApproved(review.isApproved())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
