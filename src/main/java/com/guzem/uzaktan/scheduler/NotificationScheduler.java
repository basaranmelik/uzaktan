package com.guzem.uzaktan.scheduler;

import com.guzem.uzaktan.model.Enrollment;
import com.guzem.uzaktan.model.NotificationType;
import com.guzem.uzaktan.model.User;
import com.guzem.uzaktan.repository.AssignmentRepository;
import com.guzem.uzaktan.repository.EnrollmentRepository;
import com.guzem.uzaktan.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final AssignmentRepository assignmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final NotificationService notificationService;

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
}
