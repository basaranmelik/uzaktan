package com.guzem.uzaktan.controller;

import com.guzem.uzaktan.dto.request.ZoomMeetingCreateRequest;
import com.guzem.uzaktan.dto.request.ZoomMeetingUpdateRequest;
import com.guzem.uzaktan.dto.response.ZoomMeetingResponse;
import com.guzem.uzaktan.exception.UnauthorizedActionException;
import com.guzem.uzaktan.service.CourseService;
import com.guzem.uzaktan.service.UserService;
import com.guzem.uzaktan.service.ZoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ZoomController {

    private final ZoomService zoomService;
    private final CourseService courseService;
    private final UserService userService;

    // ---- Öğretmen: toplantı listesi ----

    @GetMapping("/egitmen/zoom/kurslarim/{courseId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public String meetingList(@PathVariable Long courseId,
                              @AuthenticationPrincipal UserDetails principal,
                              Model model) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        model.addAttribute("course", courseService.findById(courseId));
        model.addAttribute("meetings", zoomService.findByCourse(courseId, userId));
        return "egitmen/zoom-toplantilari";
    }

    // ---- Öğretmen: yeni toplantı ----

    @GetMapping("/egitmen/zoom/kurslarim/{courseId}/yeni")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public String newMeetingForm(@PathVariable Long courseId, Model model) {
        model.addAttribute("course", courseService.findById(courseId));
        model.addAttribute("zoomMeetingCreateRequest", new ZoomMeetingCreateRequest());
        return "egitmen/zoom-toplanti-olustur";
    }

    @PostMapping("/egitmen/zoom/kurslarim/{courseId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public String createMeeting(@PathVariable Long courseId,
                                @Valid @ModelAttribute("zoomMeetingCreateRequest") ZoomMeetingCreateRequest request,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal UserDetails principal,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("course", courseService.findById(courseId));
            return "egitmen/zoom-toplanti-olustur";
        }
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        zoomService.createMeeting(courseId, request, userId);
        redirectAttributes.addFlashAttribute("successMessage", "Zoom toplantısı başarıyla oluşturuldu. Kayıtlı öğrencilere bildirim gönderildi.");
        return "redirect:/egitmen/zoom/kurslarim/" + courseId;
    }

    // ---- Öğretmen: toplantı düzenle ----

    @GetMapping("/egitmen/zoom/toplanti/{id}/duzenle")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public String editMeetingForm(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails principal,
                                  Model model) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        ZoomMeetingResponse meeting = zoomService.findByIdForTeacher(id, userId);
        ZoomMeetingUpdateRequest req = new ZoomMeetingUpdateRequest();
        req.setTopic(meeting.getTopic());
        req.setScheduledAt(meeting.getScheduledAt());
        req.setDurationMinutes(meeting.getDurationMinutes());
        model.addAttribute("meeting", meeting);
        model.addAttribute("zoomMeetingUpdateRequest", req);
        return "egitmen/zoom-toplanti-duzenle";
    }

    @PostMapping("/egitmen/zoom/toplanti/{id}/duzenle")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public String updateMeeting(@PathVariable Long id,
                                @Valid @ModelAttribute("zoomMeetingUpdateRequest") ZoomMeetingUpdateRequest request,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal UserDetails principal,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        if (bindingResult.hasErrors()) {
            model.addAttribute("meeting", zoomService.findByIdForTeacher(id, userId));
            return "egitmen/zoom-toplanti-duzenle";
        }
        ZoomMeetingResponse updated = zoomService.updateMeeting(id, request, userId);
        redirectAttributes.addFlashAttribute("successMessage", "Toplantı güncellendi. Kayıtlı öğrencilere bildirim gönderildi.");
        return "redirect:/egitmen/zoom/kurslarim/" + updated.getCourseId();
    }

    // ---- Öğretmen: kayıt linki ekle ----

    @PostMapping("/egitmen/zoom/toplanti/{id}/kayit")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public String addRecordingUrl(@PathVariable Long id,
                                  @RequestParam(required = false) String recordingUrl,
                                  @AuthenticationPrincipal UserDetails principal,
                                  RedirectAttributes redirectAttributes) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        ZoomMeetingResponse meeting = zoomService.findByIdForTeacher(id, userId);
        zoomService.addRecordingUrl(id, recordingUrl, userId);
        redirectAttributes.addFlashAttribute("successMessage", "Kayıt linki kaydedildi.");
        return "redirect:/egitmen/zoom/kurslarim/" + meeting.getCourseId();
    }

    // ---- Öğretmen: toplantı iptal ----

    @PostMapping("/egitmen/zoom/toplanti/{id}/iptal")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public String cancelMeeting(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails principal,
                                RedirectAttributes redirectAttributes) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        ZoomMeetingResponse meeting = zoomService.findByIdForTeacher(id, userId);
        zoomService.cancelMeeting(id, userId);
        redirectAttributes.addFlashAttribute("successMessage", "Toplantı iptal edildi. Kayıtlı öğrencilere bildirim gönderildi.");
        return "redirect:/egitmen/zoom/kurslarim/" + meeting.getCourseId();
    }

    // ---- Öğretmen: toplantıyı başlat (bildirim gönder + yönlendir) ----

    @PostMapping("/egitmen/zoom/toplanti/{id}/baslat")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public String startMeeting(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        ZoomMeetingResponse meeting = zoomService.findByIdForTeacher(id, userId);
        zoomService.startMeeting(id, userId);
        return "redirect:" + meeting.getStartUrl();
    }

    // ---- Öğrenci: katıl ----

    @GetMapping("/zoom/toplanti/{id}/katil")
    public String joinMeeting(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails principal,
                              Model model) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        try {
            model.addAttribute("meeting", zoomService.getForStudent(id, userId));
        } catch (UnauthorizedActionException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "zoom/katil";
    }

    // ---- Öğrenci: tüm derslerim ----

    @GetMapping("/zoom/derslerim")
    @PreAuthorize("isAuthenticated()")
    public String studentMeetings(@AuthenticationPrincipal UserDetails principal, Model model) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        model.addAttribute("meetings", zoomService.getUpcomingForStudent(userId));
        return "zoom/derslerim";
    }
}
