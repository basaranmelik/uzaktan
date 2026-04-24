package com.guzem.uzaktan.mapper.course;

import com.guzem.uzaktan.dto.request.QuestionCreateRequest;
import com.guzem.uzaktan.dto.response.QuestionResponse;
import com.guzem.uzaktan.model.course.Course;
import com.guzem.uzaktan.model.course.Question;
import org.springframework.stereotype.Component;

@Component
public class QuestionMapper {

    public QuestionResponse toResponse(Question q) {
        return QuestionResponse.builder()
                .id(q.getId())
                .courseId(q.getCourse().getId())
                .courseTitle(q.getCourse().getTitle())
                .questionText(q.getQuestionText())
                .optionA(q.getOptionA())
                .optionB(q.getOptionB())
                .optionC(q.getOptionC())
                .optionD(q.getOptionD())
                .optionE(q.getOptionE())
                .correctOption(q.getCorrectOption())
                .correctOptionDisplayName(q.getCorrectOption().getDisplayName())
                .explanation(q.getExplanation())
                .createdAt(q.getCreatedAt())
                .updatedAt(q.getUpdatedAt())
                .build();
    }

    public Question toEntity(QuestionCreateRequest req, Course course) {
        return Question.builder()
                .questionText(req.getQuestionText())
                .optionA(req.getOptionA())
                .optionB(req.getOptionB())
                .optionC(req.getOptionC())
                .optionD(req.getOptionD())
                .optionE(req.getOptionE())
                .correctOption(req.getCorrectOption())
                .explanation(req.getExplanation())
                .course(course)
                .build();
    }
}
