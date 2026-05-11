package com.guzem.uzaktan.controller.instructor;

import com.guzem.uzaktan.dto.request.ZoomMeetingCreateRequest;
import com.guzem.uzaktan.dto.request.ZoomMeetingUpdateRequest;
import com.guzem.uzaktan.dto.response.CourseResponse;
import com.guzem.uzaktan.dto.response.ZoomMeetingResponse;
import com.guzem.uzaktan.exception.UnauthorizedActionException;
import com.guzem.uzaktan.service.course.CourseService;
import com.guzem.uzaktan.service.instructor.ZoomService;
import com.guzem.uzaktan.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/egitmen/zoom")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
public class ZoomController {

    private final ZoomService zoomService;
    private final CourseService courseService;
    private final UserService userService;

    // ---- Öğretmen: toplantı listesi ----
    @GetMapping("/kurslarim/{courseId}")
    public String meetingList(@PathVariable Long courseId,
                              @ModelAttribute("currentUserId") Long currentUserId,
                              Model model) {
        model.addAttribute("course", courseService.findById(courseId));
        model.addAttribute("meetings", zoomService.findByCourse(courseId, currentUserId));
        model.addAttribute("instructorZoomEmail", getInstructorZoomEmail(courseId));
        return "egitmen/zoom-toplantilari";
    }

    // ---- Öğretmen: yeni toplantı ----
    @GetMapping("/kurslarim/{courseId}/yeni")
    public String newMeetingForm(@PathVariable Long courseId, Model model) {
        model.addAttribute("course", courseService.findById(courseId));
        model.addAttribute("instructorZoomEmail", getInstructorZoomEmail(courseId));
        model.addAttribute("zoomMeetingCreateRequest", new ZoomMeetingCreateRequest());
        return "egitmen/zoom-toplanti-olustur";
    }

    @PostMapping("/kurslarim/{courseId}")
    public String createMeeting(@PathVariable Long courseId,
                                @Valid @ModelAttribute("zoomMeetingCreateRequest") ZoomMeetingCreateRequest request,
                                BindingResult bindingResult,
                                @ModelAttribute("currentUserId") Long currentUserId,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("course", courseService.findById(courseId));
            model.addAttribute("instructorZoomEmail", getInstructorZoomEmail(courseId));
            return "egitmen/zoom-toplanti-olustur";
        }
        zoomService.createMeeting(courseId, request, currentUserId);
        redirectAttributes.addFlashAttribute("successMessage", "Zoom toplantısı başarıyla oluşturuldu.");
        return "redirect:/egitmen/zoom/kurslarim/" + courseId;
    }

    // ---- Öğretmen: toplantı düzenle ----
    @GetMapping("/toplanti/{id}/duzenle")
    public String editMeetingForm(@PathVariable Long id,
                                  @ModelAttribute("currentUserId") Long currentUserId,
                                  Model model) {
        ZoomMeetingResponse meeting = zoomService.findByIdForTeacher(id, currentUserId);
        ZoomMeetingUpdateRequest req = new ZoomMeetingUpdateRequest();
        req.setTopic(meeting.getTopic());
        req.setScheduledAt(meeting.getScheduledAt());
        req.setDurationMinutes(meeting.getDurationMinutes());
        model.addAttribute("meeting", meeting);
        model.addAttribute("instructorZoomEmail", getInstructorZoomEmail(meeting.getCourseId()));
        model.addAttribute("zoomMeetingUpdateRequest", req);
        return "egitmen/zoom-toplanti-duzenle";
    }

    @PostMapping("/toplanti/{id}")
    public String updateMeeting(@PathVariable Long id,
                                @Valid @ModelAttribute("zoomMeetingUpdateRequest") ZoomMeetingUpdateRequest request,
                                BindingResult bindingResult,
                                @ModelAttribute("currentUserId") Long currentUserId,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            ZoomMeetingResponse meeting = zoomService.findByIdForTeacher(id, currentUserId);
            model.addAttribute("meeting", meeting);
            model.addAttribute("instructorZoomEmail", getInstructorZoomEmail(meeting.getCourseId()));
            return "egitmen/zoom-toplanti-duzenle";
        }
        ZoomMeetingResponse updated = zoomService.updateMeeting(id, request, currentUserId);
        redirectAttributes.addFlashAttribute("successMessage", "Toplantı güncellendi.");
        return "redirect:/egitmen/zoom/kurslarim/" + updated.getCourseId();
    }

    // ---- Öğretmen: kayıt linki ekle ----
    @PostMapping("/toplanti/{id}/kayit")
    public String addRecordingUrl(@PathVariable Long id,
                                  @RequestParam(required = false) String recordingUrl,
                                  @ModelAttribute("currentUserId") Long currentUserId,
                                  RedirectAttributes redirectAttributes) {
        ZoomMeetingResponse meeting = zoomService.findByIdForTeacher(id, currentUserId);
        zoomService.addRecordingUrl(id, recordingUrl, currentUserId);
        redirectAttributes.addFlashAttribute("successMessage", "Kayıt linki kaydedildi.");
        return "redirect:/egitmen/zoom/kurslarim/" + meeting.getCourseId();
    }

    // ---- Öğretmen: toplantı iptal ----
    @PostMapping("/toplanti/{id}/iptal")
    public String cancelMeeting(@PathVariable Long id,
                                @ModelAttribute("currentUserId") Long currentUserId,
                                RedirectAttributes redirectAttributes) {
        ZoomMeetingResponse meeting = zoomService.findByIdForTeacher(id, currentUserId);
        zoomService.cancelMeeting(id, currentUserId);
        redirectAttributes.addFlashAttribute("successMessage", "Toplantı iptal edildi.");
        return "redirect:/egitmen/zoom/kurslarim/" + meeting.getCourseId();
    }

    // ---- Öğretmen: toplantıyı başlat ----
    @PostMapping("/toplanti/{id}/baslat")
    public String startMeeting(@PathVariable Long id,
                               @ModelAttribute("currentUserId") Long currentUserId) {
        ZoomMeetingResponse meeting = zoomService.findByIdForTeacher(id, currentUserId);
        zoomService.startMeeting(id, currentUserId);
        String startUrl = meeting.getStartUrl();
        if (startUrl != null && (startUrl.startsWith("https://zoom.us/") || startUrl.startsWith("https://www.zoom.us/"))) {
            return "redirect:" + startUrl;
        }
        return "redirect:/egitmen/zoom/kurslarim/" + meeting.getCourseId();
    }

    // ---- Öğretmen/Admin: toplu toplantı oluştur ----
    @PostMapping("/kurslarim/{courseId}/toplu-olustur")
    public String generateScheduledMeetings(@PathVariable Long courseId,
                                            @ModelAttribute("currentUserId") Long currentUserId,
                                            RedirectAttributes redirectAttributes) {
        courseService.findById(courseId);
        zoomService.generateScheduledMeetings(courseId);
        redirectAttributes.addFlashAttribute("successMessage",
                "Zoom toplantıları arka planda oluşturuluyor. Birkaç dakika sonra sayfayı yenileyiniz.");
        return "redirect:/egitmen/zoom/kurslarim/" + courseId;
    }

    // ---- Öğrenci: katıl (ayrı auth, ayrı prefix) ----
    @GetMapping("/zoom/toplanti/{id}/katil")
    @PreAuthorize("hasRole('USER')")
    public String joinMeeting(@PathVariable Long id,
                              @ModelAttribute("currentUserId") Long currentUserId,
                              Model model) {
        try {
            model.addAttribute("meeting", zoomService.getForStudent(id, currentUserId));
        } catch (UnauthorizedActionException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "zoom/katil";
    }

    // ---- Öğrenci: tüm derslerim (ayrı auth, ayrı prefix) ----
    @GetMapping("/zoom/derslerim")
    @PreAuthorize("hasRole('USER')")
    public String studentMeetings(@ModelAttribute("currentUserId") Long currentUserId, Model model) {
        model.addAttribute("meetings", zoomService.getUpcomingForStudent(currentUserId));
        model.addAttribute("pastMeetings", zoomService.getAllForStudent(currentUserId).stream()
                .filter(m -> m.isPast() && m.getRecordingUrl() != null && !m.getRecordingUrl().isBlank())
                .toList());
        return "zoom/derslerim";
    }

    private String getInstructorZoomEmail(Long courseId) {
        CourseResponse course = courseService.findById(courseId);
        if (course.getInstructorId() != null) {
            return userService.findById(course.getInstructorId()).getZoomEmail();
        }
        return null;
    }
}
