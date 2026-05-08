package com.guzem.uzaktan.dto.response;

import com.guzem.uzaktan.model.course.CorrectOption;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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
    private Integer moduleIndex;
    @Setter
    private String moduleTitle;
    private String imagePath;
    private String videoPath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
