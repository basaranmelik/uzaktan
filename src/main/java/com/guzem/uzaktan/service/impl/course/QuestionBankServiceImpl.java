package com.guzem.uzaktan.service.impl.course;

import com.guzem.uzaktan.dto.CurriculumModule;
import com.guzem.uzaktan.dto.request.QuestionCreateRequest;
import com.guzem.uzaktan.dto.request.QuestionUpdateRequest;
import com.guzem.uzaktan.dto.response.QuestionResponse;
import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.exception.UnauthorizedActionException;
import com.guzem.uzaktan.mapper.course.CourseMapper;
import com.guzem.uzaktan.mapper.course.QuestionMapper;
import com.guzem.uzaktan.model.common.Role;
import com.guzem.uzaktan.model.common.User;
import com.guzem.uzaktan.model.course.Course;
import com.guzem.uzaktan.model.course.Question;
import com.guzem.uzaktan.repository.course.CourseRepository;
import com.guzem.uzaktan.repository.course.QuestionRepository;
import com.guzem.uzaktan.repository.user.UserRepository;
import com.guzem.uzaktan.service.common.FileStorageService;
import com.guzem.uzaktan.service.course.QuestionBankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class QuestionBankServiceImpl implements QuestionBankService {

    private final QuestionRepository questionRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final QuestionMapper questionMapper;
    private final CourseMapper courseMapper;
    private final FileStorageService fileStorageService;

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final long MAX_VIDEO_SIZE = 50 * 1024 * 1024; // 50 MB

    @Override
    public QuestionResponse createQuestion(Long courseId, QuestionCreateRequest request, Long requestingUserId) {
        Course course = loadCourse(courseId);
        checkTeacherOrAdmin(course, requestingUserId);
        Question question = questionMapper.toEntity(request, course);

        question.setImagePath(uploadFile(request.getImage(), course, "img", MAX_IMAGE_SIZE));
        question.setVideoPath(uploadFile(request.getVideo(), course, "vid", MAX_VIDEO_SIZE));

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
        question.setModuleIndex(request.getModuleIndex());

        String newImage = uploadFile(request.getImage(), question.getCourse(), "img", MAX_IMAGE_SIZE);
        if (newImage != null) {
            deleteOldFile(question.getImagePath());
            question.setImagePath(newImage);
        }
        String newVideo = uploadFile(request.getVideo(), question.getCourse(), "vid", MAX_VIDEO_SIZE);
        if (newVideo != null) {
            deleteOldFile(question.getVideoPath());
            question.setVideoPath(newVideo);
        }

        Question saved = questionRepository.save(question);
        return questionMapper.toResponse(saved);
    }

    @Override
    public void deleteQuestion(Long questionId, Long requestingUserId) {
        Question question = loadQuestion(questionId);
        checkTeacherOrAdmin(question.getCourse(), requestingUserId);
        deleteOldFile(question.getImagePath());
        deleteOldFile(question.getVideoPath());
        questionRepository.delete(question);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> findByCourse(Long courseId) {
        Course course = loadCourse(courseId);
        List<CurriculumModule> modules = courseMapper.parseCurriculumModules(course.getManualCurriculum());

        return questionRepository.findByCourseIdOrderByCreatedAtDesc(courseId)
                .stream()
                .map(q -> {
                    QuestionResponse resp = questionMapper.toResponse(q);
                    if (q.getModuleIndex() != null && q.getModuleIndex() < modules.size()) {
                        resp.setModuleTitle(modules.get(q.getModuleIndex()).getTitle());
                    }
                    return resp;
                })
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

    private String uploadFile(MultipartFile file, Course course, String prefix, long maxSize) {
        if (file == null || file.isEmpty()) return null;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("Dosya boyutu en fazla " + (maxSize / (1024 * 1024)) + " MB olabilir.");
        }
        try {
            if ("vid".equals(prefix)) {
                return fileStorageService.storeVideo(file, course.getId(), course.getTitle(), prefix);
            }
            return fileStorageService.storeCourseImage(file, course.getId(), course.getTitle());
        } catch (IOException e) {
            log.error("Dosya yüklenemedi: {}", e.getMessage());
            throw new RuntimeException("Dosya yüklenirken hata oluştu.", e);
        }
    }

    private void deleteOldFile(String path) {
        if (path != null) {
            try {
                fileStorageService.delete(path);
            } catch (Exception e) {
                log.warn("Eski dosya silinemedi ({}): {}", path, e.getMessage());
            }
        }
    }
}
