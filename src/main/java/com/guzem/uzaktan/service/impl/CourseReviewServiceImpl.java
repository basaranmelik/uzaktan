package com.guzem.uzaktan.service.impl;

import com.guzem.uzaktan.dto.request.CourseReviewRequest;
import com.guzem.uzaktan.dto.response.CourseReviewResponse;
import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.mapper.CourseReviewMapper;
import com.guzem.uzaktan.model.Course;
import com.guzem.uzaktan.model.CourseReview;
import com.guzem.uzaktan.model.User;
import com.guzem.uzaktan.repository.CourseRepository;
import com.guzem.uzaktan.repository.CourseReviewRepository;
import com.guzem.uzaktan.repository.UserRepository;
import com.guzem.uzaktan.service.CourseReviewService;
import com.guzem.uzaktan.service.CourseService;
import com.guzem.uzaktan.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CourseReviewServiceImpl implements CourseReviewService {

    private final CourseReviewRepository courseReviewRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseReviewMapper courseReviewMapper;
    private final CourseService courseService;
    private final NotificationService notificationService;

    public CourseReviewServiceImpl(CourseReviewRepository courseReviewRepository,
                                   CourseRepository courseRepository,
                                   UserRepository userRepository,
                                   CourseReviewMapper courseReviewMapper,
                                   @Lazy CourseService courseService,
                                   NotificationService notificationService) {
        this.courseReviewRepository = courseReviewRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.courseReviewMapper = courseReviewMapper;
        this.courseService = courseService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public CourseReviewResponse addReview(Long courseId, Long userId, CourseReviewRequest request) {
        if (hasUserReviewed(courseId, userId)) {
            throw new IllegalStateException("Bu kurs için zaten bir yorum yaptınız.");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Kurs", "id", courseId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "id", userId));

        CourseReview review = CourseReview.builder()
                .course(course)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .isApproved(false)
                .build();

        CourseReview saved = courseReviewRepository.save(review);

        // Admin'lere yeni yorum bildirimi
        String userName = user.getFirstName() + " " + user.getLastName();
        userRepository.findByRole(com.guzem.uzaktan.model.Role.ADMIN).forEach(admin ->
                notificationService.create(admin, com.guzem.uzaktan.model.NotificationType.REVIEW_PENDING,
                        "Yeni Yorum Onay Bekliyor",
                        userName + ", \"" + course.getTitle() + "\" kursuna yorum yaptı.",
                        "/admin/yorumlar"));

        return courseReviewMapper.toResponse(saved);
    }

    @Override
    public List<CourseReviewResponse> getApprovedReviewsByCourse(Long courseId) {
        return courseReviewRepository.findByCourseIdAndIsApprovedTrueOrderByCreatedAtDesc(courseId)
                .stream()
                .map(courseReviewMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseReviewResponse> getPendingReviews() {
        return courseReviewRepository.findByIsApprovedFalseOrderByCreatedAtDesc()
                .stream()
                .map(courseReviewMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void approveReview(Long reviewId) {
        CourseReview review = courseReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Yorum", "id", reviewId));

        if (!review.isApproved()) {
            review.setApproved(true);
            courseReviewRepository.save(review);
            courseService.updateCourseRating(review.getCourse().getId());
            notificationService.create(review.getUser(), com.guzem.uzaktan.model.NotificationType.REVIEW_APPROVED,
                    "Yorumunuz Yayınlandı",
                    "\"" + review.getCourse().getTitle() + "\" kursuna yazdığınız yorum onaylanarak yayına alındı.",
                    "/egitimler/" + review.getCourse().getId());
        }
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        CourseReview review = courseReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Yorum", "id", reviewId));

        Long courseId = review.getCourse().getId();
        boolean wasApproved = review.isApproved();

        courseReviewRepository.delete(review);

        if (wasApproved) {
            courseService.updateCourseRating(courseId);
        }
    }

    @Override
    public boolean hasUserReviewed(Long courseId, Long userId) {
        return courseReviewRepository.existsByCourseIdAndUserId(courseId, userId);
    }

    @Override
    public long countPendingReviews() {
        return courseReviewRepository.countByIsApprovedFalse();
    }

    @Override
    public long countAllReviews() {
        return courseReviewRepository.count();
    }
}
