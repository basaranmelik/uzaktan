package com.guzem.uzaktan.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResultResponse {
    private int score;
    private int totalQuestions;
    private boolean passed;
    private boolean certificateIssued;
    private String certificateCode;
    private List<QuizAnswerDetail> details;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuizAnswerDetail {
        private Long questionId;
        private String questionText;
        private String selectedOption;
        private String correctOption;
        private boolean correct;
        private String explanation;
    }
}
