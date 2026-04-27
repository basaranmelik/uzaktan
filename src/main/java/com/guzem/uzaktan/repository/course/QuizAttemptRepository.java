package com.guzem.uzaktan.repository.course;

import com.guzem.uzaktan.model.course.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    @Query("SELECT COUNT(q) FROM QuizAttempt q WHERE q.user.id = :userId AND q.course.id = :courseId AND q.attemptDate >= :since")
    long countTodayAttempts(@Param("userId") Long userId,
                            @Param("courseId") Long courseId,
                            @Param("since") LocalDateTime since);

    Optional<QuizAttempt> findTopByUserIdAndCourseIdAndPassedTrueOrderByAttemptDateDesc(Long userId, Long courseId);

    List<QuizAttempt> findByUserIdAndCourseIdOrderByAttemptDateDesc(Long userId, Long courseId);
}
