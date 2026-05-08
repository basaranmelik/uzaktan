package com.guzem.uzaktan.service.impl.instructor;

import com.guzem.uzaktan.dto.request.ZoomMeetingCreateRequest;
import com.guzem.uzaktan.dto.request.ZoomMeetingUpdateRequest;
import com.guzem.uzaktan.dto.response.ZoomMeetingResponse;
import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.exception.UnauthorizedActionException;
import com.guzem.uzaktan.model.course.Course;
import com.guzem.uzaktan.model.instructor.ZoomMeeting;
import com.guzem.uzaktan.model.instructor.ZoomMeetingStatus;
import com.guzem.uzaktan.model.common.User;
import com.guzem.uzaktan.model.user.NotificationType;
import com.guzem.uzaktan.repository.course.CourseRepository;
import com.guzem.uzaktan.repository.course.EnrollmentRepository;
import com.guzem.uzaktan.repository.instructor.ZoomMeetingRepository;
import com.guzem.uzaktan.service.common.EmailService;
import com.guzem.uzaktan.service.course.EnrollmentService;
import com.guzem.uzaktan.service.user.NotificationService;
import com.guzem.uzaktan.service.instructor.ZoomService;
import com.guzem.uzaktan.service.client.ZoomApiClient;
import com.guzem.uzaktan.model.course.CourseType;
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
import java.util.stream.Collectors;

@Slf4j
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

        String zoomUserId = course.getInstructor().getZoomEmail();
        if (zoomUserId == null || zoomUserId.isBlank()) {
            throw new IllegalStateException("Zoom hesabınız tanımlanmamış. Lütfen profilinize Zoom e-posta adresinizi ekleyin.");
        }

        ZoomApiClient.ZoomApiMeetingRequest apiRequest = new ZoomApiClient.ZoomApiMeetingRequest();
        apiRequest.setTopic(request.getTopic());
        apiRequest.setStartTime(request.getScheduledAt().format(ZOOM_DT_FORMAT));
        apiRequest.setDuration(request.getDurationMinutes());

        ZoomApiClient.ZoomApiMeetingResponse apiResponse = zoomApiClient.createMeeting(zoomUserId, apiRequest);

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
        } catch (Exception e) {
            log.warn("Zoom API güncelleme başarısız (meetingId={}): {}", meeting.getZoomMeetingId(), e.getMessage());
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
        } catch (Exception e) {
            log.warn("Zoom API silme başarısız (meetingId={}): {}", meeting.getZoomMeetingId(), e.getMessage());
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

    @Override
    @Transactional
    public void processRecordingCompleted(String zoomMeetingId) {
        ZoomMeeting meeting = zoomMeetingRepository.findByZoomMeetingId(zoomMeetingId).orElse(null);
        if (meeting == null) {
            log.warn("Zoom webhook: toplantı bulunamadı, zoomMeetingId={}", zoomMeetingId);
            return;
        }

        ZoomApiClient.ZoomRecordingListResponse recordings;
        try {
            recordings = zoomApiClient.getMeetingRecordings(zoomMeetingId);
        } catch (Exception e) {
            log.error("Zoom recordings API hatası, meetingId={}: {}", zoomMeetingId, e.getMessage());
            return;
        }

        String playUrl = recordings.getRecordingFiles().stream()
                .filter(f -> "MP4".equalsIgnoreCase(f.getFileType())
                        && "completed".equalsIgnoreCase(f.getStatus()))
                .map(ZoomApiClient.ZoomRecordingFile::getPlayUrl)
                .filter(url -> url != null && !url.isBlank())
                .findFirst()
                .orElse(null);

        if (playUrl == null) {
            log.warn("Zoom webhook: tamamlanmış MP4 kaydı bulunamadı, zoomMeetingId={}", zoomMeetingId);
            return;
        }

        if (playUrl.length() > 500) {
            playUrl = playUrl.substring(0, 500);
        }

        meeting.setRecordingUrl(playUrl);
        zoomMeetingRepository.save(meeting);

        notifyEnrolledStudents(meeting.getCourse(), NotificationType.RECORDING_READY,
                "Ders Kaydı Hazır",
                "\"" + meeting.getCourse().getTitle() + "\" kursunun \""
                        + meeting.getTopic() + "\" ders kaydı izlemeye hazır.",
                "/zoom/toplanti/" + meeting.getId() + "/katil");

        log.info("Zoom kaydı otomatik eklendi, meetingId={}, internalId={}", zoomMeetingId, meeting.getId());
    }

    @Override
    @Transactional
    public void markMeetingAsStarted(String zoomMeetingId) {
        ZoomMeeting meeting = zoomMeetingRepository.findByZoomMeetingId(zoomMeetingId).orElse(null);
        if (meeting == null) {
            log.warn("Zoom webhook: meeting.started — toplantı bulunamadı, zoomMeetingId={}", zoomMeetingId);
            return;
        }
        if (meeting.isHostJoined()) {
            return;
        }
        meeting.setHostJoined(true);
        meeting.setStartedAt(LocalDateTime.now());
        zoomMeetingRepository.save(meeting);
        log.info("Zoom meeting.started kaydedildi, internalId={}, zoomMeetingId={}", meeting.getId(), zoomMeetingId);
    }

    @Override
    @Async("taskExecutor")
    public void generateScheduledMeetings(Long courseId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) return;

        // Guard: sadece HYBRID ve FACE_TO_FACE
        if (course.getCourseType() != CourseType.HYBRID
                && course.getCourseType() != CourseType.FACE_TO_FACE) {
            return;
        }

        // Guard: schedule alanları dolu olmalı
        if (course.getStartDate() == null || course.getEndDate() == null
                || course.getScheduleDays() == null || course.getScheduleDays().isBlank()
                || course.getScheduleStartTime() == null || course.getScheduleStartTime().isBlank()
                || course.getScheduleEndTime() == null || course.getScheduleEndTime().isBlank()) {
            log.info("Kurs #{} için zamanlama bilgileri eksik, Zoom toplantıları oluşturulmadı.", courseId);
            return;
        }

        // Guard: instructor zoom email'i olmalı
        User instructor = course.getInstructor();
        if (instructor == null || instructor.getZoomEmail() == null || instructor.getZoomEmail().isBlank()) {
            log.info("Kurs #{} eğitmeninin Zoom e-postası tanımlı değil, toplantılar oluşturulmadı.", courseId);
            return;
        }

        // Guard: duplikasyon kontrolü
        long activeCount = zoomMeetingRepository.countByCourseIdAndStatusNot(courseId, ZoomMeetingStatus.CANCELLED);
        if (activeCount > 0) {
            log.info("Kurs #{} için {} mevcut toplantı var, toplu oluşturma atlandı.", courseId, activeCount);
            return;
        }

        String zoomUserId = instructor.getZoomEmail();

        Set<DayOfWeek> days = Arrays.stream(course.getScheduleDays().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toSet());

        LocalTime startTime = LocalTime.parse(course.getScheduleStartTime());
        LocalTime endTime = LocalTime.parse(course.getScheduleEndTime());
        int durationMinutes = (int) Duration.between(startTime, endTime).toMinutes();

        if (durationMinutes <= 0) {
            log.warn("Kurs #{} için geçersiz ders süresi: {} dakika", courseId, durationMinutes);
            return;
        }

        LocalDate current = course.getStartDate();
        LocalDate end = course.getEndDate();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        int created = 0;
        int failed = 0;

        while (!current.isAfter(end)) {
            if (days.contains(current.getDayOfWeek())) {
                LocalDateTime scheduledAt = LocalDateTime.of(current, startTime);
                String topic = course.getTitle() + " - " + current.format(dateFormat);

                try {
                    ZoomApiClient.ZoomApiMeetingRequest apiRequest = new ZoomApiClient.ZoomApiMeetingRequest();
                    apiRequest.setTopic(topic);
                    apiRequest.setStartTime(scheduledAt.format(ZOOM_DT_FORMAT));
                    apiRequest.setDuration(durationMinutes);

                    ZoomApiClient.ZoomApiMeetingResponse apiResponse =
                            zoomApiClient.createMeeting(zoomUserId, apiRequest);

                    ZoomMeeting meeting = ZoomMeeting.builder()
                            .course(course)
                            .zoomMeetingId(apiResponse.getId())
                            .topic(topic)
                            .joinUrl(apiResponse.getJoinUrl())
                            .startUrl(apiResponse.getStartUrl())
                            .password(apiResponse.getPassword())
                            .scheduledAt(scheduledAt)
                            .durationMinutes(durationMinutes)
                            .build();

                    zoomMeetingRepository.save(meeting);
                    created++;

                    Thread.sleep(100); // Zoom rate limit: ~10 req/s
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn("Zoom toplantı oluşturma işlemi kesildi, kurs #{}", courseId);
                    break;
                } catch (Exception e) {
                    failed++;
                    log.error("Zoom toplantısı oluşturulamadı, kurs #{}, tarih={}: {}",
                            courseId, current, e.getMessage());
                }
            }
            current = current.plusDays(1);
        }

        log.info("Kurs #{} için {} Zoom toplantısı oluşturuldu, {} başarısız.", courseId, created, failed);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAllMeetings() {
        return zoomMeetingRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countMissedMeetings() {
        return zoomMeetingRepository.countByScheduledAtBeforeAndStatusAndHostJoinedFalse(
                LocalDateTime.now(), ZoomMeetingStatus.SCHEDULED);
    }

    @Override
    @Transactional(readOnly = true)
    public long countRecordedMeetings() {
        return zoomMeetingRepository.countByRecordingUrlIsNotNull();
    }

    @Override
    @Transactional(readOnly = true)
    public long countStartedMeetings() {
        return zoomMeetingRepository.countByHostJoinedTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ZoomMeetingResponse> findAllForAdmin() {
        return zoomMeetingRepository.findAllWithCourse()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ZoomMeetingResponse> findAllForAdminByCourse(Long courseId) {
        return zoomMeetingRepository.findByCourseIdOrderByScheduledAtDesc(courseId)
                .stream()
                .map(this::toResponse)
                .toList();
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
                .hostJoined(m.isHostJoined())
                .startedAt(m.getStartedAt())
                .instructorName(m.getCourse().getInstructor() != null
                        ? m.getCourse().getInstructor().getFirstName() + " " + m.getCourse().getInstructor().getLastName()
                        : null)
                .createdAt(m.getCreatedAt())
                .build();
    }
}
