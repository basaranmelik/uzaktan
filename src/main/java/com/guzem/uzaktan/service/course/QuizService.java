package com.guzem.uzaktan.service.course;

import com.guzem.uzaktan.dto.response.QuizAttemptResponse;
import com.guzem.uzaktan.dto.response.QuizQuestionResponse;
import com.guzem.uzaktan.dto.response.QuizResultResponse;

import java.util.List;
import java.util.Map;

public interface QuizService {

    List<QuizQuestionResponse> startQuiz(Long userId, Long courseId);

    QuizResultResponse submitQuiz(Long userId, Long courseId, Map<Long, String> answers);

    boolean canTakeQuiz(Long userId, Long courseId);

    int getRemainingAttempts(Long userId, Long courseId);

    boolean hasPassedQuiz(Long userId, Long courseId);

    List<QuizAttemptResponse> getAttemptHistory(Long userId, Long courseId);
}
