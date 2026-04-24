package com.guzem.uzaktan.dto.response;

import com.guzem.uzaktan.model.course.CorrectOption;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class QuestionResponse {

    private Long id;
    private Long courseId;
    private String courseTitle;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String optionE;
    private CorrectOption correctOption;
    private String correctOptionDisplayName;
    private String explanation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
