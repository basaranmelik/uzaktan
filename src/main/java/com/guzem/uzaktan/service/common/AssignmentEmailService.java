package com.guzem.uzaktan.service.common;

import com.guzem.uzaktan.model.admin.Assignment;
import com.guzem.uzaktan.model.admin.AssignmentSubmission;
import com.guzem.uzaktan.model.common.User;

import java.time.LocalDateTime;

/**
 * Email notifications for assignment and submission lifecycle events.
 */
public interface AssignmentEmailService {

    void sendAssignmentSubmittedToTeacher(User teacher, AssignmentSubmission submission);

    void sendAssignmentGradedToStudent(AssignmentSubmission submission);

    void sendAssignmentDueReminder(User student, String assignmentTitle, String courseTitle, LocalDateTime dueDate);

    void sendNewAssignmentNotification(User student, Assignment assignment);
}
