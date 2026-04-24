package com.guzem.uzaktan.service.course;

import com.guzem.uzaktan.dto.request.QuestionCreateRequest;
import com.guzem.uzaktan.dto.request.QuestionUpdateRequest;
import com.guzem.uzaktan.dto.response.QuestionResponse;

import java.util.List;

public interface QuestionBankService {

    QuestionResponse createQuestion(Long courseId, QuestionCreateRequest request, Long requestingUserId);

    QuestionResponse updateQuestion(Long questionId, QuestionUpdateRequest request, Long requestingUserId);

    void deleteQuestion(Long questionId, Long requestingUserId);

    List<QuestionResponse> findByCourse(Long courseId);

    QuestionResponse findById(Long questionId, Long requestingUserId);

    long countByCourse(Long courseId);
}
