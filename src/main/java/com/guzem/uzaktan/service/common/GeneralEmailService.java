package com.guzem.uzaktan.service.common;

import com.guzem.uzaktan.dto.request.ContactRequest;
import com.guzem.uzaktan.model.common.User;

/**
 * General-purpose emails: contact form, course announcements, teacher onboarding.
 */
public interface GeneralEmailService {

    void sendContactEmail(ContactRequest request);

    void sendCourseAnnouncement(User student, String courseTitle, String subject, String messageText);

    void sendTeacherWelcomeEmail(String toEmail, String fullName, String tempPassword);
}
