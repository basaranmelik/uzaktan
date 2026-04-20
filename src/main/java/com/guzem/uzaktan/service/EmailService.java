package com.guzem.uzaktan.service;

import com.guzem.uzaktan.model.AssignmentSubmission;
import com.guzem.uzaktan.model.User;
import com.guzem.uzaktan.model.ZoomMeeting;

import java.time.LocalDateTime;

public interface EmailService {

    /** Öğrenci ödev yüklediğinde eğitmene bildirim */
    void sendAssignmentSubmittedToTeacher(User teacher, AssignmentSubmission submission);

    /** Ödev notlandırıldığında öğrenciye bildirim */
    void sendAssignmentGradedToStudent(AssignmentSubmission submission);

    /** Ödev son teslim günü geldiğinde öğrenciye hatırlatma */
    void sendAssignmentDueReminder(User student, String assignmentTitle, String courseTitle, LocalDateTime dueDate);

    /** Yeni Zoom toplantısı oluşturulduğunda öğrenciye bildirim */
    void sendMeetingScheduled(User student, ZoomMeeting meeting);

    /** Zoom toplantısı iptal edildiğinde öğrenciye bildirim */
    void sendMeetingCancelled(User student, ZoomMeeting meeting);

    /** Zoom toplantısı 30 dk öncesinde hatırlatma */
    void sendMeetingReminder(User student, ZoomMeeting meeting);

    /** İletişim formundan gelen mesajı yöneticiye gönderir */
    void sendContactEmail(com.guzem.uzaktan.dto.request.ContactRequest request);

    /** Eğitmen tarafından kurs öğrencilerine gönderilen toplu duyuru */
    void sendCourseAnnouncement(User student, String courseTitle, String subject, String messageText);

    /** Kursa yeni ödev eklendiğinde öğrenciye bildirim */
    void sendNewAssignmentNotification(User student, com.guzem.uzaktan.model.Assignment assignment);
}
