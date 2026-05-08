package com.guzem.uzaktan.controller.instructor;

import com.guzem.uzaktan.dto.CurriculumModule;
import com.guzem.uzaktan.dto.request.QuestionCreateRequest;
import com.guzem.uzaktan.dto.request.QuestionUpdateRequest;
import com.guzem.uzaktan.dto.response.CourseResponse;
import com.guzem.uzaktan.dto.response.QuestionImportResult;
import com.guzem.uzaktan.dto.response.QuestionResponse;
import com.guzem.uzaktan.exception.UnauthorizedActionException;
import com.guzem.uzaktan.service.course.CourseService;
import com.guzem.uzaktan.service.course.QuestionBankService;
import com.guzem.uzaktan.service.course.QuestionExcelService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.guzem.uzaktan.model.course.CorrectOption;
import com.guzem.uzaktan.model.common.Role;
import org.apache.tika.Tika;
import org.springframework.security.core.context.SecurityContextHolder;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    private final QuestionExcelService questionExcelService;

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
        model.addAttribute("curriculumModules", course.getCurriculumModules() != null ? course.getCurriculumModules() : Collections.emptyList());
        model.addAttribute("moduleOptions", buildModuleOptionsHtml(course));
        model.addAttribute("correctOptions", Arrays.asList(CorrectOption.values()));
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
            CourseResponse course = courseService.findById(courseId);
            model.addAttribute("course", course);
            model.addAttribute("curriculumModules", course.getCurriculumModules() != null ? course.getCurriculumModules() : Collections.emptyList());
            model.addAttribute("moduleOptions", buildModuleOptionsHtml(course));
            model.addAttribute("correctOptions", Arrays.asList(CorrectOption.values()));
            return "egitmen/soru-form";
        }
        try {
            questionBankService.createQuestion(courseId, request, currentUserId);
            redirectAttributes.addFlashAttribute("successMessage", "Soru başarıyla eklendi.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/egitmen/kurslarim/" + courseId + "/soru-bankasi/yeni";
        }
        return "redirect:/egitmen/kurslarim/" + courseId + "/soru-bankasi";
    }

    @GetMapping("/sorular/{id}/duzenle")
    public String editQuestionForm(@PathVariable Long id,
                                   @ModelAttribute("currentUserId") Long currentUserId,
                                   Model model) {
        QuestionResponse question = questionBankService.findById(id, currentUserId);
        model.addAttribute("question", question);
        CourseResponse editCourse = courseService.findById(question.getCourseId());
        model.addAttribute("course", editCourse);
        model.addAttribute("curriculumModules", editCourse.getCurriculumModules() != null ? editCourse.getCurriculumModules() : Collections.emptyList());
        model.addAttribute("moduleOptions", buildModuleOptionsHtml(editCourse));
        model.addAttribute("correctOptions", Arrays.asList(CorrectOption.values()));
        QuestionUpdateRequest dto = new QuestionUpdateRequest();
        dto.setQuestionText(question.getQuestionText());
        dto.setOptionA(question.getOptionA());
        dto.setOptionB(question.getOptionB());
        dto.setOptionC(question.getOptionC());
        dto.setOptionD(question.getOptionD());
        dto.setOptionE(question.getOptionE());
        dto.setCorrectOption(question.getCorrectOption());
        dto.setExplanation(question.getExplanation());
        dto.setModuleIndex(question.getModuleIndex());
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
            QuestionResponse question = questionBankService.findById(id, currentUserId);
            model.addAttribute("question", question);
            CourseResponse updateCourse = courseService.findById(question.getCourseId());
            model.addAttribute("course", updateCourse);
            model.addAttribute("curriculumModules", updateCourse.getCurriculumModules() != null ? updateCourse.getCurriculumModules() : Collections.emptyList());
            model.addAttribute("moduleOptions", buildModuleOptionsHtml(updateCourse));
            model.addAttribute("correctOptions", Arrays.asList(CorrectOption.values()));
            return "egitmen/soru-duzenle";
        }
        QuestionResponse updated;
        try {
            updated = questionBankService.updateQuestion(id, request, currentUserId);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/egitmen/sorular/" + id + "/duzenle";
        }
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

    // ── Excel Toplu İşlemler ──

    @PostMapping("/kurslarim/{courseId}/soru-bankasi/excel-yukle")
    public String importExcel(@PathVariable Long courseId,
                              @RequestParam("file") MultipartFile file,
                              @ModelAttribute("currentUserId") Long currentUserId,
                              RedirectAttributes redirectAttributes) {
        CourseResponse course = courseService.findById(courseId);
        if (!isCourseOwnerOrAdmin(course, currentUserId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bu kursa erişim yetkiniz bulunmamaktadır.");
            return "redirect:/egitmen/panel";
        }

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lütfen bir Excel dosyası seçin.");
            return "redirect:/egitmen/kurslarim/" + courseId + "/soru-bankasi";
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".xlsx")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Sadece .xlsx dosyaları desteklenmektedir.");
            return "redirect:/egitmen/kurslarim/" + courseId + "/soru-bankasi";
        }

        if (!isValidExcelMime(file)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Geçersiz Excel dosyası. Lütfen geçerli bir .xlsx dosyası yükleyin.");
            return "redirect:/egitmen/kurslarim/" + courseId + "/soru-bankasi";
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            redirectAttributes.addFlashAttribute("errorMessage", "Excel dosyası en fazla 10 MB olabilir.");
            return "redirect:/egitmen/kurslarim/" + courseId + "/soru-bankasi";
        }

        QuestionImportResult result = questionExcelService.importFromExcel(courseId, file, currentUserId);

        if (result.getErrorCount() == 0) {
            String msg = result.getSuccessCount() + " soru başarıyla eklendi.";
            if (result.getUpdateCount() > 0) {
                msg += " " + result.getUpdateCount() + " soru güncellendi.";
            }
            redirectAttributes.addFlashAttribute("successMessage", msg);
        } else {
            redirectAttributes.addFlashAttribute("importResult", result);
            if (result.getSuccessCount() > 0 || result.getUpdateCount() > 0) {
                redirectAttributes.addFlashAttribute("successMessage",
                        (result.getSuccessCount() + result.getUpdateCount()) + " soru işlendi, "
                                + result.getErrorCount() + " satırda hata var.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Hiçbir soru eklenemedi. " + result.getErrorCount() + " satırda hata var.");
            }
        }

        return "redirect:/egitmen/kurslarim/" + courseId + "/soru-bankasi";
    }

    @GetMapping("/kurslarim/{courseId}/soru-bankasi/excel-indir")
    public void exportExcel(@PathVariable Long courseId,
                            @ModelAttribute("currentUserId") Long currentUserId,
                            HttpServletResponse response) throws IOException {
        CourseResponse course = courseService.findById(courseId);
        if (!isCourseOwnerOrAdmin(course, currentUserId)) {
            throw new UnauthorizedActionException("Bu kursa erişim yetkiniz bulunmamaktadır.");
        }

        String safeTitle = course.getTitle()
                .replaceAll("[^a-zA-Z0-9ğüşöçıİĞÜŞÖÇ\\s]", "")
                .trim().replace(' ', '_');

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + safeTitle + "_sorular.xlsx\"");
        questionExcelService.exportToExcel(courseId, currentUserId, response.getOutputStream());
    }

    @GetMapping("/soru-bankasi/excel-sablon")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"soru_bankasi_sablon.xlsx\"");
        questionExcelService.generateTemplate(response.getOutputStream());
    }

    private boolean isCourseOwnerOrAdmin(CourseResponse course, Long userId) {
        return (course.getInstructorId() != null && course.getInstructorId().equals(userId))
                || currentUserIsAdmin();
    }

    private boolean currentUserIsAdmin() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(g -> g.getAuthority().equals(Role.ADMIN.getAuthority()));
    }

    private boolean isValidExcelMime(MultipartFile file) {
        try {
            String mimeType = new Tika().detect(file.getBytes());
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(mimeType);
        } catch (IOException e) {
            return false;
        }
    }

    private String buildModuleOptionsHtml(CourseResponse course) {
        List<CurriculumModule> modules = course.getCurriculumModules();
        if (modules == null || modules.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < modules.size(); i++) {
            sb.append("<option value=\"").append(i).append("\">")
              .append(modules.get(i).getTitle())
              .append("</option>");
        }
        return sb.toString();
    }
}
