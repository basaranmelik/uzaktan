package com.guzem.uzaktan.service;

import com.guzem.uzaktan.model.*;
import com.guzem.uzaktan.repository.AssignmentRepository;
import com.guzem.uzaktan.repository.AssignmentSubmissionRepository;
import com.guzem.uzaktan.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssignmentSchedulerService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final NotificationService notificationService;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /**
     * Her saat başı: son 24 saat içinde teslim tarihi dolacak ödevler için bildirim gönder.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void sendDueSoonNotifications() {
        LocalDateTime now = LocalDateTime.now();
        List<Assignment> dueSoon = assignmentRepository.findDueBetween(now, now.plusHours(24))
                .stream()
                .filter(a -> a.getDueSoonNotifiedAt() == null)
                .toList();

        for (Assignment assignment : dueSoon) {
            List<User> recipients = enrollmentRepository.findActiveUsersWithoutSubmission(
                    assignment.getCourse().getId(), assignment.getId());
            for (User user : recipients) {
                notificationService.create(user, NotificationType.ASSIGNMENT_DUE_SOON,
                        "Ödev Son 24 Saat",
                        "\"" + assignment.getTitle() + "\" ödevinin son teslim tarihi yaklaşıyor: "
                                + assignment.getDueDate().format(FMT),
                        "/odevlerim/" + assignment.getId());
            }
            if (!recipients.isEmpty()) {
                log.info("Due-soon bildirim gönderildi: ödev={}, alıcı sayısı={}", assignment.getId(), recipients.size());
            }
            assignment.setDueSoonNotifiedAt(now);
        }
    }

    /**
     * Her saat 30. dakikada: süresi dolmuş ve teslim edilmemiş ödevlere otomatik 0 ver.
     */
    @Scheduled(cron = "0 30 * * * *")
    @Transactional
    public void autoZeroOverdueAssignments() {
        LocalDateTime now = LocalDateTime.now();
        List<Assignment> overdue = assignmentRepository.findAllOverdue(now);

        for (Assignment assignment : overdue) {
            List<User> usersWithoutSubmission = enrollmentRepository.findActiveUsersWithoutSubmission(
                    assignment.getCourse().getId(), assignment.getId());
            for (User user : usersWithoutSubmission) {
                AssignmentSubmission submission = AssignmentSubmission.builder()
                        .assignment(assignment)
                        .user(user)
                        .score(0)
                        .feedback("Son teslim tarihi geçtiği için otomatik olarak 0 puan verildi.")
                        .status(SubmissionStatus.GRADED)
                        .gradedAt(now)
                        .build();
                submissionRepository.save(submission);
                notificationService.create(user, NotificationType.ASSIGNMENT_GRADED,
                        "Ödev Teslim Edilmedi",
                        "\"" + assignment.getTitle() + "\" ödevini teslim etmediğiniz için 0 puan verildi.",
                        "/odevlerim/" + assignment.getId());
                log.info("Otomatik 0: ödev={}, kullanıcı={}", assignment.getId(), user.getId());
            }
        }
    }
}
