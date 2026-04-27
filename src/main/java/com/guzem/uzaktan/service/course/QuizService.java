package com.guzem.uzaktan.service.course;

import com.guzem.uzaktan.dto.response.QuizQuestionResponse;
import com.guzem.uzaktan.dto.response.QuizResultResponse;
import com.guzem.uzaktan.model.course.QuizAttempt;

import java.util.List;
import java.util.Map;

public interface QuizService {

    List<QuizQuestionResponse> startQuiz(Long userId, Long courseId);

    QuizResultResponse submitQuiz(Long userId, Long courseId, Map<Long, String> answers);

    boolean canTakeQuiz(Long userId, Long courseId);

    int getRemainingAttempts(Long userId, Long courseId);

    boolean hasPassedQuiz(Long userId, Long courseId);

    List<QuizAttempt> getAttemptHistory(Long userId, Long courseId);
}
