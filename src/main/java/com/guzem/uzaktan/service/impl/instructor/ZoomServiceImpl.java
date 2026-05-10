package com.guzem.uzaktan.service.impl.instructor;

import com.guzem.uzaktan.dto.request.ZoomMeetingCreateRequest;
import com.guzem.uzaktan.dto.request.ZoomMeetingUpdateRequest;
import com.guzem.uzaktan.dto.response.ZoomMeetingResponse;
import com.guzem.uzaktan.model.course.Course;
import com.guzem.uzaktan.model.instructor.ZoomMeeting;
import com.guzem.uzaktan.model.user.NotificationType;
import com.guzem.uzaktan.repository.course.EnrollmentRepository;
import com.guzem.uzaktan.repository.instructor.ZoomMeetingRepository;
import com.guzem.uzaktan.service.client.ZoomApiClient;
import com.guzem.uzaktan.service.instructor.ZoomAdminService;
import com.guzem.uzaktan.service.instructor.ZoomMeetingManagementService;
import com.guzem.uzaktan.service.instructor.ZoomService;
import com.guzem.uzaktan.service.instructor.ZoomStudentService;
import com.guzem.uzaktan.service.user.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZoomServiceImpl implements ZoomService {

    private final ZoomMeetingManagementService managementService;
    private final ZoomStudentService studentService;
    private final ZoomAdminService adminService;
    private final ZoomMeetingRepository zoomMeetingRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final NotificationService notificationService;
    private final ZoomApiClient zoomApiClient;

    // ---- Delegated management methods ----
    @Override public ZoomMeetingResponse createMeeting(Long courseId, ZoomMeetingCreateRequest request, Long teacherUserId) { return managementService.createMeeting(courseId, request, teacherUserId); }
    @Override public ZoomMeetingResponse updateMeeting(Long meetingId, ZoomMeetingUpdateRequest request, Long teacherUserId) { return managementService.updateMeeting(meetingId, request, teacherUserId); }
    @Override public void cancelMeeting(Long meetingId, Long teacherUserId) { managementService.cancelMeeting(meetingId, teacherUserId); }
    @Override public void startMeeting(Long meetingId, Long teacherUserId) { managementService.startMeeting(meetingId, teacherUserId); }
    @Override public void addRecordingUrl(Long meetingId, String recordingUrl, Long teacherUserId) { managementService.addRecordingUrl(meetingId, recordingUrl, teacherUserId); }
    @Override public List<ZoomMeetingResponse> findByCourse(Long courseId, Long teacherUserId) { return managementService.findByCourse(courseId, teacherUserId); }
    @Override public ZoomMeetingResponse findByIdForTeacher(Long meetingId, Long teacherUserId) { return managementService.findByIdForTeacher(meetingId, teacherUserId); }
    @Override public void generateScheduledMeetings(Long courseId) { managementService.generateScheduledMeetings(courseId); }

    // ---- Delegated student methods ----
    @Override public ZoomMeetingResponse getForStudent(Long meetingId, Long studentUserId) { return studentService.getForStudent(meetingId, studentUserId); }
    @Override public List<ZoomMeetingResponse> getUpcomingForStudent(Long studentUserId) { return studentService.getUpcomingForStudent(studentUserId); }
    @Override public List<ZoomMeetingResponse> getAllForStudent(Long studentUserId) { return studentService.getAllForStudent(studentUserId); }

    // ---- Delegated admin methods ----
    @Override public long countAllMeetings() { return adminService.countAllMeetings(); }
    @Override public long countMissedMeetings() { return adminService.countMissedMeetings(); }
    @Override public long countRecordedMeetings() { return adminService.countRecordedMeetings(); }
    @Override public long countStartedMeetings() { return adminService.countStartedMeetings(); }
    @Override public List<ZoomMeetingResponse> findAllForAdmin() { return adminService.findAllForAdmin(); }
    @Override public List<ZoomMeetingResponse> findAllForAdminByCourse(Long courseId) { return adminService.findAllForAdminByCourse(courseId); }

    // ---- Webhook Processing ----
    @Override
    @Transactional
    public void processRecordingCompleted(String zoomMeetingId) {
        ZoomMeeting meeting = zoomMeetingRepository.findByZoomMeetingId(zoomMeetingId).orElse(null);
        if (meeting == null) { log.warn("Zoom webhook: toplantı bulunamadı, zoomMeetingId={}", zoomMeetingId); return; }
        try {
            var recordings = zoomApiClient.getMeetingRecordings(zoomMeetingId);
            String playUrl = recordings.getRecordingFiles().stream()
                    .filter(f -> "MP4".equalsIgnoreCase(f.getFileType()) && "completed".equalsIgnoreCase(f.getStatus()))
                    .map(ZoomApiClient.ZoomRecordingFile::getPlayUrl)
                    .filter(url -> url != null && !url.isBlank()).findFirst().orElse(null);
            if (playUrl == null) { log.warn("Zoom webhook: tamamlanmış MP4 kaydı bulunamadı, zoomMeetingId={}", zoomMeetingId); return; }
            meeting.setRecordingUrl(playUrl.length() > 500 ? playUrl.substring(0, 500) : playUrl);
            zoomMeetingRepository.save(meeting);
            notifyEnrolledStudents(meeting.getCourse(), NotificationType.RECORDING_READY, "Ders Kaydı Hazır",
                    "\"" + meeting.getCourse().getTitle() + "\" kursunun \"" + meeting.getTopic() + "\" ders kaydı izlemeye hazır.",
                    "/zoom/toplanti/" + meeting.getId() + "/katil");
            log.info("Zoom kaydı otomatik eklendi, meetingId={}, internalId={}", zoomMeetingId, meeting.getId());
        } catch (Exception e) { log.error("Zoom recordings API hatası, meetingId={}: {}", zoomMeetingId, e.getMessage(), e); }
    }

    @Override
    @Transactional
    public void markMeetingAsStarted(String zoomMeetingId) {
        ZoomMeeting meeting = zoomMeetingRepository.findByZoomMeetingId(zoomMeetingId).orElse(null);
        if (meeting == null) { log.warn("Zoom webhook: meeting.started — toplantı bulunamadı, zoomMeetingId={}", zoomMeetingId); return; }
        if (meeting.isHostJoined()) return;
        meeting.setHostJoined(true);
        meeting.setStartedAt(LocalDateTime.now());
        zoomMeetingRepository.save(meeting);
        log.info("Zoom meeting.started kaydedildi, internalId={}, zoomMeetingId={}", meeting.getId(), zoomMeetingId);
    }

    // ---- Shared helpers ----
    private void notifyEnrolledStudents(Course course, NotificationType type, String title, String message, String link) {
        enrollmentRepository.findActiveEnrollmentsForCourse(course.getId())
                .forEach(e -> notificationService.create(e.getUser(), type, title, message, link));
    }
}
