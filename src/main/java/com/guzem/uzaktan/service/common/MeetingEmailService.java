package com.guzem.uzaktan.service.common;

import com.guzem.uzaktan.model.common.User;
import com.guzem.uzaktan.model.instructor.ZoomMeeting;

/**
 * Email notifications for Zoom meeting lifecycle events.
 */
public interface MeetingEmailService {

    void sendMeetingScheduled(User student, ZoomMeeting meeting);

    void sendMeetingCancelled(User student, ZoomMeeting meeting);

    void sendMeetingReminder(User student, ZoomMeeting meeting);
}
