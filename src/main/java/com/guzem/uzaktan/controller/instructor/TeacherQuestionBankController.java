package com.guzem.uzaktan.controller.instructor;

import com.guzem.uzaktan.dto.request.QuestionCreateRequest;
import com.guzem.uzaktan.dto.request.QuestionUpdateRequest;
import com.guzem.uzaktan.dto.response.CourseResponse;
import com.guzem.uzaktan.dto.response.QuestionResponse;
import com.guzem.uzaktan.service.course.CourseService;
import com.guzem.uzaktan.service.course.QuestionBankService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Eğitmen soru bankası yönetimi — soru CRUD işlemleri.
 * URL prefix: /egitmen
 */
@Controller
@RequestMapping("/egitmen")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
public class TeacherQuestionBankController {

    private final CourseService courseService;
    private final QuestionBankService questionBankService;

    @GetMapping("/kurslarim/{courseId}/soru-bankasi")
    public String questionBank(@PathVariable Long courseId,
                               @ModelAttribute("currentUserId") Long currentUserId,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        CourseResponse course = courseService.findById(courseId);
        if (!isCourseOwnerOrAdmin(course, currentUserId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bu kursa erişim yetkiniz bulunmamaktadır.");
            return "redirect:/egitmen/panel";
        }
        model.addAttribute("course", course);
        model.addAttribute("questions", questionBankService.findByCourse(courseId));
        return "egitmen/soru-bankasi";
    }

    @GetMapping("/kurslarim/{courseId}/soru-bankasi/yeni")
    public String newQuestionForm(@PathVariable Long courseId,
                                  @ModelAttribute("currentUserId") Long currentUserId,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        CourseResponse course = courseService.findById(courseId);
        if (!isCourseOwnerOrAdmin(course, currentUserId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bu kursa erişim yetkiniz bulunmamaktadır.");
            return "redirect:/egitmen/panel";
        }
        model.addAttribute("course", course);
        model.addAttribute("questionCreateRequest", new QuestionCreateRequest());
        return "egitmen/soru-form";
    }

    @PostMapping("/kurslarim/{courseId}/soru-bankasi")
    public String createQuestion(@PathVariable Long courseId,
                                 @Valid @ModelAttribute("questionCreateRequest") QuestionCreateRequest request,
                                 BindingResult bindingResult,
                                 @ModelAttribute("currentUserId") Long currentUserId,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("course", courseService.findById(courseId));
            return "egitmen/soru-form";
        }
        questionBankService.createQuestion(courseId, request, currentUserId);
        redirectAttributes.addFlashAttribute("successMessage", "Soru başarıyla eklendi.");
        return "redirect:/egitmen/kurslarim/" + courseId + "/soru-bankasi";
    }

    @GetMapping("/sorular/{id}/duzenle")
    public String editQuestionForm(@PathVariable Long id,
                                   @ModelAttribute("currentUserId") Long currentUserId,
                                   Model model) {
        QuestionResponse question = questionBankService.findById(id, currentUserId);
        model.addAttribute("question", question);
        QuestionUpdateRequest dto = new QuestionUpdateRequest();
        dto.setQuestionText(question.getQuestionText());
        dto.setOptionA(question.getOptionA());
        dto.setOptionB(question.getOptionB());
        dto.setOptionC(question.getOptionC());
        dto.setOptionD(question.getOptionD());
        dto.setOptionE(question.getOptionE());
        dto.setCorrectOption(question.getCorrectOption());
        dto.setExplanation(question.getExplanation());
        model.addAttribute("questionUpdateRequest", dto);
        return "egitmen/soru-duzenle";
    }

    @PostMapping("/sorular/{id}")
    public String updateQuestion(@PathVariable Long id,
                                 @Valid @ModelAttribute("questionUpdateRequest") QuestionUpdateRequest request,
                                 BindingResult bindingResult,
                                 @ModelAttribute("currentUserId") Long currentUserId,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("question", questionBankService.findById(id, currentUserId));
            return "egitmen/soru-duzenle";
        }
        QuestionResponse updated = questionBankService.updateQuestion(id, request, currentUserId);
        redirectAttributes.addFlashAttribute("successMessage", "Soru güncellendi.");
        return "redirect:/egitmen/kurslarim/" + updated.getCourseId() + "/soru-bankasi";
    }

    @PostMapping("/sorular/{id}/sil")
    public String deleteQuestion(@PathVariable Long id,
                                 @ModelAttribute("currentUserId") Long currentUserId,
                                 RedirectAttributes redirectAttributes) {
        QuestionResponse question = questionBankService.findById(id, currentUserId);
        Long courseId = question.getCourseId();
        questionBankService.deleteQuestion(id, currentUserId);
        redirectAttributes.addFlashAttribute("successMessage", "Soru silindi.");
        return "redirect:/egitmen/kurslarim/" + courseId + "/soru-bankasi";
    }

    private boolean isCourseOwnerOrAdmin(CourseResponse course, Long userId) {
        return course.getInstructorId() != null && course.getInstructorId().equals(userId);
    }
}
