package com.guzem.uzaktan.service.impl.instructor;

import com.guzem.uzaktan.dto.response.ZoomMeetingResponse;
import com.guzem.uzaktan.model.instructor.ZoomMeeting;
import com.guzem.uzaktan.model.instructor.ZoomMeetingStatus;
import com.guzem.uzaktan.mapper.instructor.ZoomMeetingMapper;
import com.guzem.uzaktan.repository.instructor.ZoomMeetingRepository;
import com.guzem.uzaktan.service.instructor.ZoomAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ZoomAdminServiceImpl implements ZoomAdminService {

    private final ZoomMeetingRepository zoomMeetingRepository;
    private final ZoomMeetingMapper zoomMeetingMapper;

    @Override
    public long countAllMeetings() { return zoomMeetingRepository.count(); }

    @Override
    public long countMissedMeetings() {
        return zoomMeetingRepository.countByScheduledAtBeforeAndStatusAndHostJoinedFalse(
                LocalDateTime.now(), ZoomMeetingStatus.SCHEDULED);
    }

    @Override
    public long countRecordedMeetings() { return zoomMeetingRepository.countByRecordingUrlIsNotNull(); }

    @Override
    public long countStartedMeetings() { return zoomMeetingRepository.countByHostJoinedTrue(); }

    @Override
    public List<ZoomMeetingResponse> findAllForAdmin() {
        return zoomMeetingRepository.findAllWithCourse().stream().map(zoomMeetingMapper::toResponse).toList();
    }

    @Override
    public List<ZoomMeetingResponse> findAllForAdminByCourse(Long courseId) {
        return zoomMeetingRepository.findByCourseIdOrderByScheduledAtDesc(courseId)
                .stream().map(zoomMeetingMapper::toResponse).toList();
    }
}
