package com.guzem.uzaktan.service.impl.course;

import com.guzem.uzaktan.dto.request.CourseReviewRequest;
import com.guzem.uzaktan.dto.response.CourseReviewResponse;
import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.mapper.course.CourseReviewMapper;
import com.guzem.uzaktan.model.course.Course;
import com.guzem.uzaktan.model.course.CourseReview;
import com.guzem.uzaktan.model.common.User;
import com.guzem.uzaktan.model.common.Role;
import com.guzem.uzaktan.model.user.NotificationType;
import com.guzem.uzaktan.repository.course.CourseRepository;
import com.guzem.uzaktan.repository.course.CourseReviewRepository;
import com.guzem.uzaktan.repository.user.UserRepository;
import com.guzem.uzaktan.service.course.CourseReviewService;
import com.guzem.uzaktan.service.user.NotificationService;
import com.guzem.uzaktan.event.ReviewApprovedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseReviewServiceImpl implements CourseReviewService {

    private final CourseReviewRepository courseReviewRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseReviewMapper courseReviewMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;
    private final CacheManager cacheManager;

    @Override
    @Transactional
    @CacheEvict(value = "courseReviews", key = "#courseId")
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
        userRepository.findByRole(com.guzem.uzaktan.model.common.Role.ADMIN).forEach(admin ->
                notificationService.create(admin, com.guzem.uzaktan.model.user.NotificationType.REVIEW_PENDING,
                        "Yeni Yorum Onay Bekliyor",
                        userName + ", \"" + course.getTitle() + "\" kursuna yorum yaptı.",
                        "/admin/yorumlar"));

        return courseReviewMapper.toResponse(saved);
    }

    @Override
    @Cacheable(value = "courseReviews", key = "#courseId")
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
            eventPublisher.publishEvent(new ReviewApprovedEvent(review.getCourse().getId()));
            notificationService.create(review.getUser(), com.guzem.uzaktan.model.user.NotificationType.REVIEW_APPROVED,
                    "Yorumunuz Yayınlandı",
                    "\"" + review.getCourse().getTitle() + "\" kursuna yazdığınız yorum onaylanarak yayına alındı.",
                    "/egitimler/" + review.getCourse().getId());
        }
        var cache = cacheManager.getCache("courseReviews");
        if (cache != null) cache.evict(review.getCourse().getId());
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
            eventPublisher.publishEvent(new ReviewApprovedEvent(courseId));
        }
        var cache = cacheManager.getCache("courseReviews");
        if (cache != null) cache.evict(courseId);
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
