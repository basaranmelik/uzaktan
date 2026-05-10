package com.guzem.uzaktan.service.common;

/**
 * Composed email service — combines assignment, meeting, and general email operations.
 * Callers that only need one domain should inject the narrower interface directly.
 */
public interface EmailService extends AssignmentEmailService, MeetingEmailService, GeneralEmailService {
}
