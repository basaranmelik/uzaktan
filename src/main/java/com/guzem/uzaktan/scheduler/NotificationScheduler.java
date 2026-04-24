package com.guzem.uzaktan.scheduler;

import com.guzem.uzaktan.model.course.Enrollment;
import com.guzem.uzaktan.model.user.NotificationType;
import com.guzem.uzaktan.model.common.User;
import com.guzem.uzaktan.model.instructor.ZoomMeeting;
import com.guzem.uzaktan.repository.admin.AssignmentRepository;
import com.guzem.uzaktan.repository.course.EnrollmentRepository;
import com.guzem.uzaktan.repository.instructor.ZoomMeetingRepository;
import com.guzem.uzaktan.service.common.EmailService;
import com.guzem.uzaktan.service.user.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final AssignmentRepository assignmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ZoomMeetingRepository zoomMeetingRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    /**
     * Her gün 08:00'de çalışır.
     * Son teslim tarihi yarın olan ödevler için, henüz teslim yapmayan
     * aktif öğrencilere hatırlatma bildirimi gönderir.
     */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void sendAssignmentDueReminders() {
        LocalDateTime tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime tomorrowEnd   = tomorrowStart.plusDays(1);

        assignmentRepository.findDueBetween(tomorrowStart, tomorrowEnd).forEach(assignment -> {
            Long courseId      = assignment.getCourse().getId();
            Long assignmentId  = assignment.getId();

            List<User> users = enrollmentRepository.findActiveUsersWithoutSubmission(courseId, assignmentId);
            for (User user : users) {
                notificationService.create(
                        user,
                        NotificationType.ASSIGNMENT_DUE_SOON,
                        "Ödev Son Teslim Günü Yarın",
                        "\"" + assignment.getTitle() + "\" ödevinin son teslim tarihi yarın. Henüz teslim yapmadınız!",
                        "/panom"
                );
                emailService.sendAssignmentDueReminder(
                        user,
                        assignment.getTitle(),
                        assignment.getCourse().getTitle(),
                        assignment.getDueDate()
                );
            }
        });
    }

    /**
     * Her gün 08:00'de çalışır.
     * Bitiş tarihi bugün olan kursların aktif kayıtlı öğrencilerine
     * "kurs tamamlandı" bildirimi gönderir.
     */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void sendCourseEndedNotifications() {
        LocalDate today = LocalDate.now();

        List<Enrollment> enrollments = enrollmentRepository.findActiveEnrollmentsForCoursesEndingOn(today);
        for (Enrollment enrollment : enrollments) {
            notificationService.create(
                    enrollment.getUser(),
                    NotificationType.COURSE_ENDED,
                    "Kursunuz Tamamlandı",
                    "\"" + enrollment.getCourse().getTitle() + "\" kursu bugün sona erdi. Başarılar!",
                    "/panom"
            );
        }
    }

    /**
     * Her 15 dakikada bir çalışır.
     * 30 dakika içinde başlayacak Zoom toplantıları için
     * aktif kayıtlı öğrencilere hatırlatma gönderir.
     */
    @Scheduled(cron = "0 */15 * * * *")
    @Transactional
    public void sendMeetingReminders() {
        LocalDateTime from = LocalDateTime.now().plusMinutes(25);
        LocalDateTime to   = LocalDateTime.now().plusMinutes(35);

        List<ZoomMeeting> meetings = zoomMeetingRepository.findScheduledBetween(from, to);
        for (ZoomMeeting meeting : meetings) {
            List<User> users = enrollmentRepository
                    .findActiveEnrollmentsForCourse(meeting.getCourse().getId())
                    .stream().map(Enrollment::getUser).collect(Collectors.toList());
            for (User user : users) {
                notificationService.create(user, NotificationType.MEETING_REMINDER,
                        "Canlı Ders 30 Dakika Sonra Başlıyor",
                        "\"" + meeting.getTopic() + "\" dersi 30 dakika sonra başlıyor!",
                        "/zoom/toplanti/" + meeting.getId() + "/katil");
                emailService.sendMeetingReminder(user, meeting);
            }
        }
    }
}
