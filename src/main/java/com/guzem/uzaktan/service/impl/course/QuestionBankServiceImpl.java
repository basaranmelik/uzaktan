package com.guzem.uzaktan.service.impl.course;

import com.guzem.uzaktan.dto.request.QuestionCreateRequest;
import com.guzem.uzaktan.dto.request.QuestionUpdateRequest;
import com.guzem.uzaktan.dto.response.QuestionResponse;
import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.exception.UnauthorizedActionException;
import com.guzem.uzaktan.mapper.course.QuestionMapper;
import com.guzem.uzaktan.model.common.Role;
import com.guzem.uzaktan.model.common.User;
import com.guzem.uzaktan.model.course.Course;
import com.guzem.uzaktan.model.course.Question;
import com.guzem.uzaktan.repository.course.CourseRepository;
import com.guzem.uzaktan.repository.course.QuestionRepository;
import com.guzem.uzaktan.repository.user.UserRepository;
import com.guzem.uzaktan.service.course.QuestionBankService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionBankServiceImpl implements QuestionBankService {

    private final QuestionRepository questionRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final QuestionMapper questionMapper;

    @Override
    public QuestionResponse createQuestion(Long courseId, QuestionCreateRequest request, Long requestingUserId) {
        Course course = loadCourse(courseId);
        checkTeacherOrAdmin(course, requestingUserId);
        Question question = questionMapper.toEntity(request, course);
        Question saved = questionRepository.save(question);
        return questionMapper.toResponse(saved);
    }

    @Override
    public QuestionResponse updateQuestion(Long questionId, QuestionUpdateRequest request, Long requestingUserId) {
        Question question = loadQuestion(questionId);
        checkTeacherOrAdmin(question.getCourse(), requestingUserId);
        question.setQuestionText(request.getQuestionText());
        question.setOptionA(request.getOptionA());
        question.setOptionB(request.getOptionB());
        question.setOptionC(request.getOptionC());
        question.setOptionD(request.getOptionD());
        question.setOptionE(request.getOptionE());
        question.setCorrectOption(request.getCorrectOption());
        question.setExplanation(request.getExplanation());
        Question saved = questionRepository.save(question);
        return questionMapper.toResponse(saved);
    }

    @Override
    public void deleteQuestion(Long questionId, Long requestingUserId) {
        Question question = loadQuestion(questionId);
        checkTeacherOrAdmin(question.getCourse(), requestingUserId);
        questionRepository.delete(question);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> findByCourse(Long courseId) {
        return questionRepository.findByCourseIdOrderByCreatedAtDesc(courseId)
                .stream()
                .map(questionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionResponse findById(Long questionId, Long requestingUserId) {
        Question question = loadQuestion(questionId);
        checkTeacherOrAdmin(question.getCourse(), requestingUserId);
        return questionMapper.toResponse(question);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByCourse(Long courseId) {
        return questionRepository.countByCourseId(courseId);
    }

    private void checkTeacherOrAdmin(Course course, Long requestingUserId) {
        User user = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "id", requestingUserId));
        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isInstructor = course.getInstructor() != null
                && course.getInstructor().getId().equals(requestingUserId);
        if (!isAdmin && !isInstructor) {
            throw new UnauthorizedActionException("Bu işlem için yetkiniz bulunmamaktadır.");
        }
    }

    private Question loadQuestion(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Soru", "id", id));
    }

    private Course loadCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kurs", "id", id));
    }
}
