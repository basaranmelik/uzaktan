package com.guzem.uzaktan.service.impl;

import com.guzem.uzaktan.dto.request.ZoomMeetingCreateRequest;
import com.guzem.uzaktan.dto.request.ZoomMeetingUpdateRequest;
import com.guzem.uzaktan.dto.response.ZoomMeetingResponse;
import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.exception.UnauthorizedActionException;
import com.guzem.uzaktan.model.*;
import com.guzem.uzaktan.repository.CourseRepository;
import com.guzem.uzaktan.repository.EnrollmentRepository;
import com.guzem.uzaktan.repository.ZoomMeetingRepository;
import com.guzem.uzaktan.service.EmailService;
import com.guzem.uzaktan.service.EnrollmentService;
import com.guzem.uzaktan.service.NotificationService;
import com.guzem.uzaktan.service.ZoomService;
import com.guzem.uzaktan.service.client.ZoomApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ZoomServiceImpl implements ZoomService {

    private final ZoomMeetingRepository zoomMeetingRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentService enrollmentService;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final ZoomApiClient zoomApiClient;

    private static final DateTimeFormatter ZOOM_DT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    @Transactional
    public ZoomMeetingResponse createMeeting(Long courseId, ZoomMeetingCreateRequest request, Long teacherUserId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Kurs", "id", courseId));

        if (course.getInstructor() == null || !course.getInstructor().getId().equals(teacherUserId)) {
            throw new UnauthorizedActionException("Bu kurs için Zoom toplantısı oluşturma yetkiniz yok.");
        }

        ZoomApiClient.ZoomApiMeetingRequest apiRequest = new ZoomApiClient.ZoomApiMeetingRequest();
        apiRequest.setTopic(request.getTopic());
        apiRequest.setStartTime(request.getScheduledAt().format(ZOOM_DT_FORMAT));
        apiRequest.setDuration(request.getDurationMinutes());

        ZoomApiClient.ZoomApiMeetingResponse apiResponse = zoomApiClient.createMeeting(apiRequest);

        ZoomMeeting meeting = ZoomMeeting.builder()
                .course(course)
                .zoomMeetingId(apiResponse.getId())
                .topic(request.getTopic())
                .joinUrl(apiResponse.getJoinUrl())
                .startUrl(apiResponse.getStartUrl())
                .password(apiResponse.getPassword())
                .scheduledAt(request.getScheduledAt())
                .durationMinutes(request.getDurationMinutes())
                .build();

        ZoomMeetingResponse response = toResponse(zoomMeetingRepository.save(meeting));

        // Kayıtlı tüm aktif öğrencilere bildirim + e-posta gönder
        ZoomMeeting savedMeeting = zoomMeetingRepository.findById(response.getId()).orElseThrow();
        notifyEnrolledStudents(course, NotificationType.MEETING_SCHEDULED,
                "Yeni Canlı Ders Planlandı",
                "\"" + course.getTitle() + "\" kursuna yeni bir canlı ders eklendi: " + request.getTopic(),
                "/zoom/toplanti/" + response.getId() + "/katil");
        enrollmentRepository.findActiveEnrollmentsForCourse(course.getId())
                .forEach(e -> emailService.sendMeetingScheduled(e.getUser(), savedMeeting));

        return response;
    }

    @Override
    @Transactional
    public ZoomMeetingResponse updateMeeting(Long meetingId, ZoomMeetingUpdateRequest request, Long teacherUserId) {
        ZoomMeeting meeting = findMeetingById(meetingId);
        verifyTeacherOwnership(meeting, teacherUserId);

        if (meeting.getStatus() == ZoomMeetingStatus.CANCELLED) {
            throw new IllegalStateException("İptal edilmiş bir toplantı güncellenemez.");
        }

        ZoomApiClient.ZoomApiMeetingRequest apiRequest = new ZoomApiClient.ZoomApiMeetingRequest();
        apiRequest.setTopic(request.getTopic());
        apiRequest.setStartTime(request.getScheduledAt().format(ZOOM_DT_FORMAT));
        apiRequest.setDuration(request.getDurationMinutes());

        try {
            zoomApiClient.updateMeeting(meeting.getZoomMeetingId(), apiRequest);
        } catch (Exception ignored) {
            // Zoom API güncellemesi başarısız olsa bile DB'yi güncelleriz
        }

        meeting.setTopic(request.getTopic());
        meeting.setScheduledAt(request.getScheduledAt());
        meeting.setDurationMinutes(request.getDurationMinutes());

        ZoomMeetingResponse response = toResponse(zoomMeetingRepository.save(meeting));

        // Öğrencileri bilgilendir
        notifyEnrolledStudents(meeting.getCourse(), NotificationType.MEETING_SCHEDULED,
                "Canlı Ders Güncellendi",
                "\"" + meeting.getCourse().getTitle() + "\" kursundaki \"" + request.getTopic() + "\" dersi güncellendi.",
                "/zoom/toplanti/" + meetingId + "/katil");

        return response;
    }

    @Override
    @Transactional
    public void cancelMeeting(Long meetingId, Long teacherUserId) {
        ZoomMeeting meeting = findMeetingById(meetingId);
        verifyTeacherOwnership(meeting, teacherUserId);

        if (meeting.getStatus() == ZoomMeetingStatus.CANCELLED) {
            return;
        }

        try {
            zoomApiClient.deleteMeeting(meeting.getZoomMeetingId());
        } catch (Exception ignored) {
            // Zoom tarafında silinmişse devam et
        }

        meeting.setStatus(ZoomMeetingStatus.CANCELLED);
        zoomMeetingRepository.save(meeting);

        // Öğrencileri bildirim + e-posta ile bilgilendir
        notifyEnrolledStudents(meeting.getCourse(), NotificationType.MEETING_CANCELLED,
                "Canlı Ders İptal Edildi",
                "\"" + meeting.getCourse().getTitle() + "\" kursundaki \"" + meeting.getTopic() + "\" dersi iptal edildi.",
                "/panom");
        ZoomMeeting cancelledMeeting = meeting;
        enrollmentRepository.findActiveEnrollmentsForCourse(meeting.getCourse().getId())
                .forEach(e -> emailService.sendMeetingCancelled(e.getUser(), cancelledMeeting));
    }

    @Override
    @Transactional
    public void startMeeting(Long meetingId, Long teacherUserId) {
        ZoomMeeting meeting = findMeetingById(meetingId);
        verifyTeacherOwnership(meeting, teacherUserId);

        if (meeting.getStatus() == ZoomMeetingStatus.CANCELLED) {
            throw new IllegalStateException("İptal edilmiş toplantı başlatılamaz.");
        }

        notifyEnrolledStudents(meeting.getCourse(), NotificationType.MEETING_STARTED,
                "Ders Başladı!",
                "\"" + meeting.getCourse().getTitle() + "\" kursunun \"" + meeting.getTopic() + "\" dersi başladı. Hemen katılın!",
                "/zoom/toplanti/" + meetingId + "/katil");
    }

    @Override
    @Transactional
    public void addRecordingUrl(Long meetingId, String recordingUrl, Long teacherUserId) {
        ZoomMeeting meeting = findMeetingById(meetingId);
        verifyTeacherOwnership(meeting, teacherUserId);
        meeting.setRecordingUrl(recordingUrl != null && recordingUrl.isBlank() ? null : recordingUrl);
        zoomMeetingRepository.save(meeting);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ZoomMeetingResponse> findByCourse(Long courseId, Long teacherUserId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Kurs", "id", courseId));

        if (course.getInstructor() == null || !course.getInstructor().getId().equals(teacherUserId)) {
            throw new UnauthorizedActionException("Bu kursa erişim yetkiniz yok.");
        }

        return zoomMeetingRepository.findByCourseIdOrderByScheduledAtDesc(courseId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ZoomMeetingResponse getForStudent(Long meetingId, Long studentUserId) {
        ZoomMeeting meeting = findMeetingById(meetingId);

        if (!enrollmentService.isActiveEnrollment(studentUserId, meeting.getCourse().getId())) {
            throw new UnauthorizedActionException("Bu toplantıya katılmak için kursa kayıtlı olmanız gerekiyor.");
        }

        if (meeting.getStatus() == ZoomMeetingStatus.CANCELLED) {
            throw new UnauthorizedActionException("Bu toplantı iptal edilmiştir.");
        }

        return toResponse(meeting);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ZoomMeetingResponse> getUpcomingForStudent(Long studentUserId) {
        return zoomMeetingRepository.findUpcomingForStudent(studentUserId, LocalDateTime.now())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ZoomMeetingResponse> getAllForStudent(Long studentUserId) {
        return zoomMeetingRepository.findAllForStudent(studentUserId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ZoomMeetingResponse findByIdForTeacher(Long meetingId, Long teacherUserId) {
        ZoomMeeting meeting = findMeetingById(meetingId);
        verifyTeacherOwnership(meeting, teacherUserId);
        return toResponse(meeting);
    }

    // ---- Yardımcı metodlar ----

    private void notifyEnrolledStudents(Course course, NotificationType type,
                                         String title, String message, String link) {
        enrollmentRepository.findActiveEnrollmentsForCourse(course.getId())
                .forEach(e -> notificationService.create(e.getUser(), type, title, message, link));
    }

    private ZoomMeeting findMeetingById(Long meetingId) {
        return zoomMeetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("ZoomMeeting", "id", meetingId));
    }

    private void verifyTeacherOwnership(ZoomMeeting meeting, Long teacherUserId) {
        Course course = meeting.getCourse();
        if (course.getInstructor() == null || !course.getInstructor().getId().equals(teacherUserId)) {
            throw new UnauthorizedActionException("Bu toplantı üzerinde işlem yapma yetkiniz yok.");
        }
    }

    private ZoomMeetingResponse toResponse(ZoomMeeting m) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = m.getScheduledAt().plusMinutes(m.getDurationMinutes());
        boolean past = end.isBefore(now);
        boolean live = !past && m.getScheduledAt().isBefore(now);

        return ZoomMeetingResponse.builder()
                .id(m.getId())
                .topic(m.getTopic())
                .joinUrl(m.getJoinUrl())
                .startUrl(m.getStartUrl())
                .password(m.getPassword())
                .scheduledAt(m.getScheduledAt())
                .durationMinutes(m.getDurationMinutes())
                .status(m.getStatus())
                .statusDisplayName(m.getStatus().getDisplayName())
                .courseId(m.getCourse().getId())
                .courseTitle(m.getCourse().getTitle())
                .recordingUrl(m.getRecordingUrl())
                .past(past)
                .live(live)
                .createdAt(m.getCreatedAt())
                .build();
    }
}
