package com.guzem.uzaktan.controller.course;

import com.guzem.uzaktan.dto.response.QuizQuestionResponse;
import com.guzem.uzaktan.dto.response.QuizResultResponse;
import com.guzem.uzaktan.model.course.QuizAttempt;
import com.guzem.uzaktan.service.course.CertificateService;
import com.guzem.uzaktan.service.course.CourseService;
import com.guzem.uzaktan.service.course.EnrollmentService;
import com.guzem.uzaktan.service.course.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/sinav")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class QuizController {

    private final QuizService quizService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final CertificateService certificateService;

    /**
     * Sınav bilgi sayfası - kurs durumu, deneme hakları vs.
     */
    @GetMapping("/{courseId}")
    public String quizInfo(@PathVariable Long courseId,
                           @ModelAttribute("currentUserId") Long currentUserId,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        var course = courseService.findById(courseId);
        var enrollmentOpt = enrollmentService.findByUserAndCourse(currentUserId, courseId);

        if (enrollmentOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bu kursa kayıtlı değilsiniz.");
            return "redirect:/egitimler/" + courseId;
        }

        var enrollment = enrollmentOpt.get();
        model.addAttribute("course", course);
        model.addAttribute("enrollment", enrollment);
        model.addAttribute("hasPassed", quizService.hasPassedQuiz(currentUserId, courseId));
        model.addAttribute("canTakeQuiz", quizService.canTakeQuiz(currentUserId, courseId));
        model.addAttribute("remainingAttempts", quizService.getRemainingAttempts(currentUserId, courseId));

        List<QuizAttempt> history = quizService.getAttemptHistory(currentUserId, courseId);
        model.addAttribute("attemptHistory", history);

        // Sertifika varsa göster
        if (quizService.hasPassedQuiz(currentUserId, courseId)) {
            var certs = certificateService.findByUser(currentUserId);
            certs.stream()
                    .filter(c -> c.getCourseTitle().equals(course.getTitle()))
                    .findFirst()
                    .ifPresent(cert -> model.addAttribute("certificate", cert));
        }

        return "quiz/info";
    }

    /**
     * Sınavı başlat - rastgele 10 soru getir.
     */
    @GetMapping("/{courseId}/baslat")
    public String startQuiz(@PathVariable Long courseId,
                            @ModelAttribute("currentUserId") Long currentUserId,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        try {
            List<QuizQuestionResponse> questions = quizService.startQuiz(currentUserId, courseId);
            var course = courseService.findById(courseId);

            model.addAttribute("course", course);
            model.addAttribute("questions", questions);
            model.addAttribute("totalQuestions", questions.size());
            return "quiz/exam";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/sinav/" + courseId;
        }
    }

    /**
     * Sınav cevaplarını gönder.
     */
    @PostMapping("/{courseId}/gonder")
    public String submitQuiz(@PathVariable Long courseId,
                             @ModelAttribute("currentUserId") Long currentUserId,
                             @RequestParam Map<String, String> allParams,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        try {
            // "answer_123" -> "B" formatındaki cevapları parse et
            Map<Long, String> answers = new HashMap<>();
            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                if (entry.getKey().startsWith("answer_")) {
                    Long questionId = Long.parseLong(entry.getKey().substring(7));
                    answers.put(questionId, entry.getValue());
                }
            }

            QuizResultResponse result = quizService.submitQuiz(currentUserId, courseId, answers);
            var course = courseService.findById(courseId);

            model.addAttribute("course", course);
            model.addAttribute("result", result);
            return "quiz/result";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/sinav/" + courseId;
        }
    }
}
