package com.guzem.uzaktan.service.impl;

import com.guzem.uzaktan.dto.request.ZoomMeetingCreateRequest;
import com.guzem.uzaktan.dto.response.ZoomMeetingResponse;
import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.exception.UnauthorizedActionException;
import com.guzem.uzaktan.model.Course;
import com.guzem.uzaktan.model.ZoomMeeting;
import com.guzem.uzaktan.model.ZoomMeetingStatus;
import com.guzem.uzaktan.repository.CourseRepository;
import com.guzem.uzaktan.repository.ZoomMeetingRepository;
import com.guzem.uzaktan.service.EnrollmentService;
import com.guzem.uzaktan.service.ZoomService;
import com.guzem.uzaktan.service.client.ZoomApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ZoomServiceImpl implements ZoomService {

    private final ZoomMeetingRepository zoomMeetingRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentService enrollmentService;
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

        return toResponse(zoomMeetingRepository.save(meeting));
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
    public ZoomMeetingResponse findByIdForTeacher(Long meetingId, Long teacherUserId) {
        ZoomMeeting meeting = findMeetingById(meetingId);
        verifyTeacherOwnership(meeting, teacherUserId);
        return toResponse(meeting);
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
                .createdAt(m.getCreatedAt())
                .build();
    }
}
