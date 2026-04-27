package com.guzem.uzaktan.service.impl.course;

import com.guzem.uzaktan.dto.response.CertificateResponse;
import com.guzem.uzaktan.dto.response.QuizQuestionResponse;
import com.guzem.uzaktan.dto.response.QuizResultResponse;
import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.model.common.User;
import com.guzem.uzaktan.model.course.*;
import com.guzem.uzaktan.model.user.NotificationType;
import com.guzem.uzaktan.repository.course.EnrollmentRepository;
import com.guzem.uzaktan.repository.course.QuestionRepository;
import com.guzem.uzaktan.repository.course.QuizAttemptRepository;
import com.guzem.uzaktan.repository.user.UserRepository;
import com.guzem.uzaktan.repository.course.CourseRepository;
import com.guzem.uzaktan.service.course.CertificateService;
import com.guzem.uzaktan.service.course.QuizService;
import com.guzem.uzaktan.service.user.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizServiceImpl implements QuizService {

    private static final int QUIZ_QUESTION_COUNT = 10;
    private static final int PASS_THRESHOLD = 80; // %80
    private static final int MAX_DAILY_ATTEMPTS = 3;

    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CertificateService certificateService;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public List<QuizQuestionResponse> startQuiz(Long userId, Long courseId) {
        validateEligibility(userId, courseId);

        List<Question> allQuestions = questionRepository.findByCourseIdOrderByCreatedAtDesc(courseId);
        if (allQuestions.size() < QUIZ_QUESTION_COUNT) {
            throw new IllegalStateException(
                    "Bu kurs için yeterli soru bulunmamaktadır. En az " + QUIZ_QUESTION_COUNT + " soru gereklidir.");
        }

        Collections.shuffle(allQuestions);
        List<Question> selected = allQuestions.subList(0, QUIZ_QUESTION_COUNT);

        return selected.stream()
                .map(q -> QuizQuestionResponse.builder()
                        .id(q.getId())
                        .questionText(q.getQuestionText())
                        .optionA(q.getOptionA())
                        .optionB(q.getOptionB())
                        .optionC(q.getOptionC())
                        .optionD(q.getOptionD())
                        .optionE(q.getOptionE())
                        .build())
                .toList();
    }

    @Override
    public QuizResultResponse submitQuiz(Long userId, Long courseId, Map<Long, String> answers) {
        validateEligibility(userId, courseId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "id", userId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Kurs", "id", courseId));

        // Cevaplanan soruları yükle
        Set<Long> questionIds = answers.keySet();
        List<Question> questions = questionIds.stream()
                .map(qId -> questionRepository.findById(qId)
                        .orElseThrow(() -> new ResourceNotFoundException("Soru", "id", qId)))
                .toList();

        // Değerlendir
        int score = 0;
        List<QuizResultResponse.QuizAnswerDetail> details = new ArrayList<>();

        for (Question question : questions) {
            String selectedOption = answers.get(question.getId());
            boolean correct = question.getCorrectOption().name().equalsIgnoreCase(selectedOption);
            if (correct) score++;

            details.add(QuizResultResponse.QuizAnswerDetail.builder()
                    .questionId(question.getId())
                    .questionText(question.getQuestionText())
                    .selectedOption(selectedOption)
                    .correctOption(question.getCorrectOption().name())
                    .correct(correct)
                    .explanation(question.getExplanation())
                    .build());
        }

        int totalQuestions = questions.size();
        int percentage = (totalQuestions > 0) ? (score * 100) / totalQuestions : 0;
        boolean passed = percentage >= PASS_THRESHOLD;

        // Denemeyi kaydet
        QuizAttempt attempt = QuizAttempt.builder()
                .user(user)
                .course(course)
                .score(score)
                .totalQuestions(totalQuestions)
                .passed(passed)
                .build();
        quizAttemptRepository.save(attempt);

        // Başarılıysa sertifika ver
        QuizResultResponse.QuizResultResponseBuilder resultBuilder = QuizResultResponse.builder()
                .score(score)
                .totalQuestions(totalQuestions)
                .passed(passed)
                .details(details)
                .certificateIssued(false);

        if (passed) {
            CertificateResponse cert = certificateService.issueCertificate(userId, courseId);
            resultBuilder.certificateIssued(true).certificateCode(cert.getCertificateCode());
        }

        return resultBuilder.build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canTakeQuiz(Long userId, Long courseId) {
        // Zaten geçtiyse tekrar giremez
        if (hasPassedQuiz(userId, courseId)) return false;

        // Günlük deneme hakkı kontrolü
        return getRemainingAttempts(userId, courseId) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public int getRemainingAttempts(Long userId, Long courseId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long todayAttempts = quizAttemptRepository.countTodayAttempts(userId, courseId, startOfDay);
        return Math.max(0, MAX_DAILY_ATTEMPTS - (int) todayAttempts);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPassedQuiz(Long userId, Long courseId) {
        return quizAttemptRepository
                .findTopByUserIdAndCourseIdAndPassedTrueOrderByAttemptDateDesc(userId, courseId)
                .isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizAttempt> getAttemptHistory(Long userId, Long courseId) {
        return quizAttemptRepository.findByUserIdAndCourseIdOrderByAttemptDateDesc(userId, courseId);
    }

    private void validateEligibility(Long userId, Long courseId) {
        // Kayıt kontrolü
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new IllegalStateException("Bu kursa kayıtlı değilsiniz."));

        // Kurs tamamlanma kontrolü (%100)
        if (enrollment.getProgressPercentage() < 100) {
            throw new IllegalStateException(
                    "Sınava girebilmek için kursun tamamını bitirmeniz gerekmektedir. Mevcut ilerleme: %" +
                            enrollment.getProgressPercentage());
        }

        // Zaten geçtiyse
        if (hasPassedQuiz(userId, courseId)) {
            throw new IllegalStateException("Bu kursun sınavını zaten başarıyla geçtiniz.");
        }

        // Günlük deneme hakkı
        if (getRemainingAttempts(userId, courseId) <= 0) {
            throw new IllegalStateException("Bugünkü deneme hakkınız doldu. Yarın tekrar deneyebilirsiniz.");
        }
    }
}
