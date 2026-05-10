package com.guzem.uzaktan.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class QuizAttemptResponse {
    private Long id;
    private Integer score;
    private Integer totalQuestions;
    private Boolean passed;
    private LocalDateTime attemptDate;
}
