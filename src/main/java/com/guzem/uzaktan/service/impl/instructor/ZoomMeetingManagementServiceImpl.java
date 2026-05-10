package com.guzem.uzaktan.service.impl.instructor;

import com.guzem.uzaktan.dto.request.ZoomMeetingCreateRequest;
import com.guzem.uzaktan.dto.request.ZoomMeetingUpdateRequest;
import com.guzem.uzaktan.dto.response.ZoomMeetingResponse;
import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.exception.UnauthorizedActionException;
import com.guzem.uzaktan.model.common.User;
import com.guzem.uzaktan.model.course.Course;
import com.guzem.uzaktan.model.course.CourseType;
import com.guzem.uzaktan.model.instructor.ZoomMeeting;
import com.guzem.uzaktan.model.instructor.ZoomMeetingStatus;
import com.guzem.uzaktan.model.user.NotificationType;
import com.guzem.uzaktan.repository.course.CourseRepository;
import com.guzem.uzaktan.repository.course.EnrollmentRepository;
import com.guzem.uzaktan.repository.instructor.ZoomMeetingRepository;
import com.guzem.uzaktan.service.client.ZoomApiClient;
import com.guzem.uzaktan.service.common.MeetingEmailService;
import com.guzem.uzaktan.service.course.EnrollmentService;
import com.guzem.uzaktan.service.instructor.ZoomMeetingManagementService;
import com.guzem.uzaktan.service.user.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZoomMeetingManagementServiceImpl implements ZoomMeetingManagementService {

    private final ZoomMeetingRepository zoomMeetingRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentService enrollmentService;
    private final NotificationService notificationService;
    private final MeetingEmailService emailService;
    private final ZoomApiClient zoomApiClient;
    private final com.guzem.uzaktan.mapper.instructor.ZoomMeetingMapper zoomMeetingMapper;

    private static final DateTimeFormatter ZOOM_DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    @Transactional
    public ZoomMeetingResponse createMeeting(Long courseId, ZoomMeetingCreateRequest request, Long teacherUserId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new ResourceNotFoundException("Kurs", "id", courseId));
        if (course.getInstructor() == null || !course.getInstructor().getId().equals(teacherUserId)) {
            throw new UnauthorizedActionException("Bu kurs için Zoom toplantısı oluşturma yetkiniz yok.");
        }
        String zoomUserId = course.getInstructor().getZoomEmail();
        if (zoomUserId == null || zoomUserId.isBlank()) {
            throw new IllegalStateException("Zoom hesabınız tanımlanmamış.");
        }
        var apiRequest = new ZoomApiClient.ZoomApiMeetingRequest();
        apiRequest.setTopic(request.getTopic());
        apiRequest.setStartTime(request.getScheduledAt().format(ZOOM_DT_FORMAT));
        apiRequest.setDuration(request.getDurationMinutes());
        var apiResponse = zoomApiClient.createMeeting(zoomUserId, apiRequest);
        ZoomMeeting meeting = ZoomMeeting.builder()
                .course(course).zoomMeetingId(apiResponse.getId()).topic(request.getTopic())
                .joinUrl(apiResponse.getJoinUrl()).startUrl(apiResponse.getStartUrl())
                .password(apiResponse.getPassword()).scheduledAt(request.getScheduledAt())
                .durationMinutes(request.getDurationMinutes()).build();
        ZoomMeetingResponse response = zoomMeetingMapper.toResponse(zoomMeetingRepository.save(meeting));
        ZoomMeeting savedMeeting = zoomMeetingRepository.findById(response.getId()).orElseThrow();
        notifyEnrolledStudents(course, "Yeni Canlı Ders Planlandı",
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
        if (meeting.getStatus() == ZoomMeetingStatus.CANCELLED) throw new IllegalStateException("İptal edilmiş toplantı güncellenemez.");
        var apiRequest = new ZoomApiClient.ZoomApiMeetingRequest();
        apiRequest.setTopic(request.getTopic());
        apiRequest.setStartTime(request.getScheduledAt().format(ZOOM_DT_FORMAT));
        apiRequest.setDuration(request.getDurationMinutes());
        try { zoomApiClient.updateMeeting(meeting.getZoomMeetingId(), apiRequest); } catch (Exception e) { log.warn("Zoom API güncelleme başarısız: {}", e.getMessage(), e); }
        meeting.setTopic(request.getTopic()); meeting.setScheduledAt(request.getScheduledAt()); meeting.setDurationMinutes(request.getDurationMinutes());
        ZoomMeetingResponse response = zoomMeetingMapper.toResponse(zoomMeetingRepository.save(meeting));
        notifyEnrolledStudents(meeting.getCourse(), "Canlı Ders Güncellendi",
                "\"" + meeting.getCourse().getTitle() + "\" kursundaki \"" + request.getTopic() + "\" dersi güncellendi.",
                "/zoom/toplanti/" + meetingId + "/katil");
        return response;
    }

    @Override
    @Transactional
    public void cancelMeeting(Long meetingId, Long teacherUserId) {
        ZoomMeeting meeting = findMeetingById(meetingId);
        verifyTeacherOwnership(meeting, teacherUserId);
        if (meeting.getStatus() == ZoomMeetingStatus.CANCELLED) return;
        try { zoomApiClient.deleteMeeting(meeting.getZoomMeetingId()); } catch (Exception e) { log.warn("Zoom API silme başarısız: {}", e.getMessage(), e); }
        meeting.setStatus(ZoomMeetingStatus.CANCELLED); zoomMeetingRepository.save(meeting);
        notifyEnrolledStudents(meeting.getCourse(), "Canlı Ders İptal Edildi",
                "\"" + meeting.getCourse().getTitle() + "\" kursundaki \"" + meeting.getTopic() + "\" dersi iptal edildi.", "/panom");
        ZoomMeeting cancelled = meeting;
        enrollmentRepository.findActiveEnrollmentsForCourse(meeting.getCourse().getId())
                .forEach(e -> emailService.sendMeetingCancelled(e.getUser(), cancelled));
    }

    @Override
    @Transactional
    public void startMeeting(Long meetingId, Long teacherUserId) {
        ZoomMeeting meeting = findMeetingById(meetingId);
        verifyTeacherOwnership(meeting, teacherUserId);
        if (meeting.getStatus() == ZoomMeetingStatus.CANCELLED) throw new IllegalStateException("İptal edilmiş toplantı başlatılamaz.");
        notifyEnrolledStudents(meeting.getCourse(), "Ders Başladı!",
                "\"" + meeting.getCourse().getTitle() + "\" kursunun \"" + meeting.getTopic() + "\" dersi başladı. Hemen katılın!",
                "/zoom/toplanti/" + meetingId + "/katil", NotificationType.MEETING_STARTED);
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
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new ResourceNotFoundException("Kurs", "id", courseId));
        if (course.getInstructor() == null || !course.getInstructor().getId().equals(teacherUserId)) throw new UnauthorizedActionException("Bu kursa erişim yetkiniz yok.");
        return zoomMeetingRepository.findByCourseIdOrderByScheduledAtDesc(courseId).stream().map(zoomMeetingMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ZoomMeetingResponse findByIdForTeacher(Long meetingId, Long teacherUserId) {
        ZoomMeeting meeting = findMeetingById(meetingId);
        verifyTeacherOwnership(meeting, teacherUserId);
        return zoomMeetingMapper.toResponse(meeting);
    }

    @Override
    @Async("taskExecutor")
    public void generateScheduledMeetings(Long courseId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) return;
        if (course.getCourseType() != CourseType.HYBRID && course.getCourseType() != CourseType.FACE_TO_FACE) return;
        if (course.getStartDate() == null || course.getEndDate() == null || course.getScheduleDays() == null || course.getScheduleDays().isBlank()
                || course.getScheduleStartTime() == null || course.getScheduleStartTime().isBlank()
                || course.getScheduleEndTime() == null || course.getScheduleEndTime().isBlank()) {
            log.info("Kurs #{} için zamanlama bilgileri eksik.", courseId); return;
        }
        User instructor = course.getInstructor();
        if (instructor == null || instructor.getZoomEmail() == null || instructor.getZoomEmail().isBlank()) {
            log.info("Kurs #{} eğitmeninin Zoom e-postası tanımlı değil.", courseId); return;
        }
        if (zoomMeetingRepository.countByCourseIdAndStatusNot(courseId, ZoomMeetingStatus.CANCELLED) > 0) {
            log.info("Kurs #{} için mevcut toplantı var, atlandı.", courseId); return;
        }
        String zoomUserId = instructor.getZoomEmail();
        Set<DayOfWeek> days = Arrays.stream(course.getScheduleDays().split(",")).map(String::trim).filter(s -> !s.isEmpty()).map(DayOfWeek::valueOf).collect(Collectors.toSet());
        LocalTime startTime = LocalTime.parse(course.getScheduleStartTime());
        LocalTime endTime = LocalTime.parse(course.getScheduleEndTime());
        int durationMinutes = (int) Duration.between(startTime, endTime).toMinutes();
        if (durationMinutes <= 0) { log.warn("Kurs #{} için geçersiz ders süresi: {} dakika", courseId, durationMinutes); return; }
        LocalDate current = course.getStartDate();
        LocalDate end = course.getEndDate();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        int created = 0, failed = 0;
        while (!current.isAfter(end)) {
            if (days.contains(current.getDayOfWeek())) {
                try {
                    var apiReq = new ZoomApiClient.ZoomApiMeetingRequest();
                    apiReq.setTopic(course.getTitle() + " - " + current.format(df));
                    apiReq.setStartTime(LocalDateTime.of(current, startTime).format(ZOOM_DT_FORMAT));
                    apiReq.setDuration(durationMinutes);
                    var apiRes = zoomApiClient.createMeeting(zoomUserId, apiReq);
                    ZoomMeeting m = ZoomMeeting.builder().course(course).zoomMeetingId(apiRes.getId())
                            .topic(apiReq.getTopic()).joinUrl(apiRes.getJoinUrl()).startUrl(apiRes.getStartUrl())
                            .password(apiRes.getPassword()).scheduledAt(LocalDateTime.of(current, startTime)).durationMinutes(durationMinutes).build();
                    zoomMeetingRepository.save(m); created++;
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException ie) { Thread.currentThread().interrupt(); break;
                } catch (Exception e) { failed++; log.error("Zoom toplantısı oluşturulamadı: {}", e.getMessage(), e); }
            }
            current = current.plusDays(1);
        }
        log.info("Kurs #{} için {} Zoom toplantısı oluşturuldu, {} başarısız.", courseId, created, failed);
    }

    private void notifyEnrolledStudents(Course course, String title, String message, String link, NotificationType type) {
        enrollmentRepository.findActiveEnrollmentsForCourse(course.getId())
                .forEach(e -> notificationService.create(e.getUser(), type, title, message, link));
    }

    private void notifyEnrolledStudents(Course course, String title, String message, String link) {
        notifyEnrolledStudents(course, title, message, link, NotificationType.MEETING_SCHEDULED);
    }

    private ZoomMeeting findMeetingById(Long meetingId) {
        return zoomMeetingRepository.findById(meetingId).orElseThrow(() -> new ResourceNotFoundException("ZoomMeeting", "id", meetingId));
    }

    private void verifyTeacherOwnership(ZoomMeeting meeting, Long teacherUserId) {
        if (meeting.getCourse().getInstructor() == null || !meeting.getCourse().getInstructor().getId().equals(teacherUserId)) {
            throw new UnauthorizedActionException("Bu toplantı üzerinde işlem yapma yetkiniz yok.");
        }
    }
}
