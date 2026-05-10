package com.guzem.uzaktan.service.impl.instructor;

import com.guzem.uzaktan.dto.response.ZoomMeetingResponse;
import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.exception.UnauthorizedActionException;
import com.guzem.uzaktan.model.instructor.ZoomMeeting;
import com.guzem.uzaktan.model.instructor.ZoomMeetingStatus;
import com.guzem.uzaktan.mapper.instructor.ZoomMeetingMapper;
import com.guzem.uzaktan.repository.instructor.ZoomMeetingRepository;
import com.guzem.uzaktan.service.course.EnrollmentService;
import com.guzem.uzaktan.service.instructor.ZoomStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ZoomStudentServiceImpl implements ZoomStudentService {

    private final ZoomMeetingRepository zoomMeetingRepository;
    private final EnrollmentService enrollmentService;
    private final ZoomMeetingMapper zoomMeetingMapper;

    @Override
    @Transactional
    public ZoomMeetingResponse getForStudent(Long meetingId, Long studentUserId) {
        ZoomMeeting meeting = zoomMeetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("ZoomMeeting", "id", meetingId));
        if (!enrollmentService.isActiveEnrollment(studentUserId, meeting.getCourse().getId())) {
            throw new UnauthorizedActionException("Bu toplantıya katılmak için kursa kayıtlı olmanız gerekiyor.");
        }
        if (meeting.getStatus() == ZoomMeetingStatus.CANCELLED) {
            throw new UnauthorizedActionException("Bu toplantı iptal edilmiştir.");
        }
        return zoomMeetingMapper.toResponse(meeting);
    }

    @Override
    public List<ZoomMeetingResponse> getUpcomingForStudent(Long studentUserId) {
        return zoomMeetingRepository.findUpcomingForStudent(studentUserId, LocalDateTime.now())
                .stream().map(zoomMeetingMapper::toResponse).toList();
    }

    @Override
    public List<ZoomMeetingResponse> getAllForStudent(Long studentUserId) {
        return zoomMeetingRepository.findAllForStudent(studentUserId)
                .stream().map(zoomMeetingMapper::toResponse).toList();
    }
}
