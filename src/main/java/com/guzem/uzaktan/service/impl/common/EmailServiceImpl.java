package com.guzem.uzaktan.service.impl.common;

import com.guzem.uzaktan.dto.request.ContactRequest;
import com.guzem.uzaktan.model.admin.AssignmentSubmission;
import com.guzem.uzaktan.model.common.User;
import com.guzem.uzaktan.model.instructor.ZoomMeeting;
import com.guzem.uzaktan.service.common.EmailService;
import com.guzem.uzaktan.service.common.EmailTemplateBuilder;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateBuilder tpl;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final DateTimeFormatter TR_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Async
    @Override
    public void sendAssignmentSubmittedToTeacher(User teacher, AssignmentSubmission submission) {
        if (teacher == null || teacher.getEmail() == null) return;
        String subject = "Yeni Ödev Teslimi: " + submission.getAssignment().getTitle();

        String details = tpl.rowFirst("Öğrenci",
                submission.getUser().getFirstName() + " " + submission.getUser().getLastName()) +
                tpl.row("Ödev", submission.getAssignment().getTitle()) +
                tpl.row("Kurs", submission.getAssignment().getCourse().getTitle());

        String body = tpl.buildEmail(
                "Yeni Ödev Teslimi",
                "📋",
                tpl.getColorAssignment(),
                "Merhaba " + teacher.getFirstName() + " " + teacher.getLastName() + ",",
                "<b>" + submission.getUser().getFirstName() + " " + submission.getUser().getLastName()
                        + "</b> adlı öğrenciniz <b>\""
                        + submission.getAssignment().getTitle() + "\"</b> ödevini teslim etti.",
                details,
                null,
                null);
        send(teacher.getEmail(), subject, body);
    }

    @Async
    @Override
    public void sendAssignmentGradedToStudent(AssignmentSubmission submission) {
        User student = submission.getUser();
        if (student.getEmail() == null) return;
        String subject = "Ödeviniz Notlandırıldı: " + submission.getAssignment().getTitle();

        String scoreText = submission.getScore() != null
                ? submission.getScore() + " / " + submission.getAssignment().getMaxScore() + " puan"
                : "—";

        String details = tpl.rowFirst("Ödev", submission.getAssignment().getTitle()) +
                tpl.row("Notunuz", scoreText) +
                tpl.row("Kurs", submission.getAssignment().getCourse().getTitle());

        String extra = submission.getFeedback() != null && !submission.getFeedback().isBlank()
                ? tpl.feedbackBox("Öğretmen Geri Bildirimi", tpl.escapeHtml(submission.getFeedback()))
                : null;

        String body = tpl.buildEmail(
                "Ödeviniz Notlandırıldı",
                "✅",
                "#0d7a5f",
                "Merhaba " + student.getFirstName() + " " + student.getLastName() + ",",
                "Ödeviniz notlandırıldı. Aşağıda detayları görebilirsiniz.",
                details,
                extra,
                null);
        send(student.getEmail(), subject, body);
    }

    @Async
    @Override
    public void sendAssignmentDueReminder(User student, String assignmentTitle, String courseTitle, LocalDateTime dueDate) {
        if (student.getEmail() == null) return;
        String subject = "Hatırlatma: \"" + assignmentTitle + "\" ödevi yarın son gün!";

        String details = tpl.rowFirst("Ödev", assignmentTitle) +
                tpl.row("Kurs", courseTitle) +
                tpl.row("Son Tarih", "<b style=\"color:#c92a2a;\">" + dueDate.format(TR_FORMAT) + "</b>");

        String extra = tpl.alertBox("Henüz ödevinizi teslim etmediniz. Lütfen son tarihe dikkat edin.");

        String body = tpl.buildEmail(
                "Ödev Son Teslim Günü Yarın!",
                "⏰",
                tpl.getColorWarning(),
                "Merhaba " + student.getFirstName() + " " + student.getLastName() + ",",
                "<b>\"" + assignmentTitle + "\"</b> ödevinin son teslim tarihi <b>yarın</b>.",
                details,
                extra,
                null);
        send(student.getEmail(), subject, body);
    }

    @Async
    @Override
    public void sendMeetingScheduled(User student, ZoomMeeting meeting) {
        if (student.getEmail() == null) return;
        String subject = "Yeni Canlı Ders: " + meeting.getTopic();

        String details = tpl.rowFirst("Konu", meeting.getTopic()) +
                tpl.row("Kurs", meeting.getCourse().getTitle()) +
                tpl.row("Tarih &amp; Saat", meeting.getScheduledAt().format(TR_FORMAT)) +
                tpl.row("Süre", meeting.getDurationMinutes() + " dakika") +
                (meeting.getPassword() != null && !meeting.getPassword().isBlank()
                        ? tpl.row("Toplantı Şifresi", "<code style=\"background:#f1f3f5;padding:2px 6px;border-radius:4px;font-family:monospace;\">"
                                + meeting.getPassword() + "</code>")
                        : "");

        String body = tpl.buildEmail(
                "Yeni Canlı Ders Planlandı",
                "🎥",
                tpl.getColorZoom(),
                "Merhaba " + student.getFirstName() + " " + student.getLastName() + ",",
                "<b>\"" + meeting.getCourse().getTitle() + "\"</b> kursunuza yeni bir canlı ders planlandı.",
                details,
                null,
                meeting.getJoinUrl());
        send(student.getEmail(), subject, body);
    }

    @Async
    @Override
    public void sendMeetingCancelled(User student, ZoomMeeting meeting) {
        if (student.getEmail() == null) return;
        String subject = "Canlı Ders İptal Edildi: " + meeting.getTopic();

        String details = tpl.rowFirst("İptal Edilen Ders", meeting.getTopic()) +
                tpl.row("Kurs", meeting.getCourse().getTitle()) +
                tpl.row("Planlanan Tarih", meeting.getScheduledAt().format(TR_FORMAT));

        String body = tpl.buildEmail(
                "Canlı Ders İptal Edildi",
                "❌",
                tpl.getColorWarning(),
                "Merhaba " + student.getFirstName() + " " + student.getLastName() + ",",
                "<b>\"" + meeting.getCourse().getTitle() + "\"</b> kursundaki canlı ders iptal edildi.",
                details,
                "<p style=\"margin:16px 0 0;font-size:0.9rem;color:#868e96;\">"
                        + "Yeni bir ders planlandığında tekrar bilgilendirileceksiniz.</p>",
                null);
        send(student.getEmail(), subject, body);
    }

    @Async
    @Override
    public void sendMeetingReminder(User student, ZoomMeeting meeting) {
        if (student.getEmail() == null) return;
        String subject = "30 Dakika Sonra Canlı Ders: " + meeting.getTopic();

        String details = tpl.rowFirst("Konu", meeting.getTopic()) +
                tpl.row("Kurs", meeting.getCourse().getTitle()) +
                tpl.row("Başlangıç Saati", "<b>" + meeting.getScheduledAt().format(TR_FORMAT) + "</b>") +
                (meeting.getPassword() != null && !meeting.getPassword().isBlank()
                        ? tpl.row("Toplantı Şifresi", "<code style=\"background:#f1f3f5;padding:2px 6px;border-radius:4px;font-family:monospace;\">"
                                + meeting.getPassword() + "</code>")
                        : "");

        String body = tpl.buildEmail(
                "Ders 30 Dakika Sonra Başlıyor",
                "🔔",
                tpl.getColorZoom(),
                "Merhaba " + student.getFirstName() + " " + student.getLastName() + ",",
                "<b>\"" + meeting.getTopic() + "\"</b> dersiniz <b>30 dakika içinde</b> başlıyor!",
                details,
                null,
                meeting.getJoinUrl());
        send(student.getEmail(), subject, body);
    }

    @Async
    @Override
    public void sendContactEmail(ContactRequest request) {
        String subject = "İletişim Formu: " + tpl.escapeHtmlBasic(request.getTopic());

        String details = tpl.rowFirst("Ad Soyad", request.getFirstName() + " " + request.getLastName()) +
                tpl.row("E-posta", request.getEmail()) +
                tpl.row("Telefon", request.getPhone() != null ? request.getPhone() : "—") +
                tpl.row("Konu", request.getTopic()) +
                tpl.row("Tarih", LocalDateTime.now().format(TR_FORMAT));

        String body = tpl.buildEmail(
                "Yeni İletişim Mesajı",
                "✉️",
                "#113a71",
                "Yeni Bir Mesajınız Var,",
                "Web sitesi üzerindeki iletişim formundan yeni bir başvuru aldınız. Detaylar aşağıdadır:",
                details,
                tpl.messageBox("Ziyaretçi Mesajı", tpl.escapeHtml(request.getMessage())),
                null);

        send(fromAddress, subject, body);
    }

    @Async
    @Override
    public void sendCourseAnnouncement(User student, String courseTitle, String subject, String messageText) {
        if (student.getEmail() == null) return;

        String details = tpl.rowFirst("Kurs", courseTitle) +
                tpl.row("Tarih", LocalDateTime.now().format(TR_FORMAT));

        String body = tpl.buildEmail(
                "Eğitmen Duyurusu",
                "📢",
                "#113a71",
                "Merhaba " + student.getFirstName() + " " + student.getLastName() + ",",
                "<b>" + courseTitle + "</b> kursu için eğitmeniniz tarafından yeni bir duyuru yayınlandı:",
                details,
                tpl.messageBox("Duyuru: " + tpl.escapeHtml(subject), tpl.escapeHtml(messageText).replace("\n", "<br>")),
                null);

        send(student.getEmail(), subject, body);
    }

    @Async
    @Override
    public void sendNewAssignmentNotification(User student, com.guzem.uzaktan.model.admin.Assignment assignment) {
        if (student.getEmail() == null) return;
        String subject = "Yeni Ödev Yayınlandı: " + assignment.getTitle();

        String details = tpl.rowFirst("Ödev", assignment.getTitle()) +
                tpl.row("Kurs", assignment.getCourse().getTitle()) +
                tpl.row("Son Teslim Tarihi", assignment.getDueDate().format(TR_FORMAT));

        String body = tpl.buildEmail(
                "Yeni Ödev Yayınlandı",
                "📝",
                tpl.getColorAssignment(),
                "Merhaba " + student.getFirstName() + " " + student.getLastName() + ",",
                "<b>\"" + assignment.getCourse().getTitle() + "\"</b> kursunuzda yeni bir ödev yayınlandı.",
                details,
                null,
                baseUrl + "/panom");
        send(student.getEmail(), subject, body);
    }

    @Async
    @Override
    public void sendTeacherWelcomeEmail(String toEmail, String fullName, String tempPassword) {
        if (toEmail == null) return;
        String subject = "GUZEM Eğitmen Hesabınız Oluşturuldu";

        String details = tpl.rowFirst("Ad Soyad", fullName) +
                tpl.row("E-posta", toEmail) +
                tpl.row("Geçici Şifre",
                        "<code style=\"background:#f1f3f5;padding:2px 8px;border-radius:4px;" +
                        "font-family:monospace;font-size:14px;letter-spacing:0.05em;\">"
                        + tpl.escapeHtmlBasic(tempPassword) + "</code>");

        String extra = tpl.messageBox("Önemli",
                "İlk girişinizde bu geçici şifreyi kullanın. " +
                "Sisteme girdikten sonra yeni bir şifre belirlemeniz istenecektir.");

        String body = tpl.buildEmail(
                "Hoş Geldiniz",
                "🎓",
                "#0b2a5b",
                "Merhaba " + tpl.escapeHtmlBasic(fullName) + ",",
                "GUZEM Uzaktan Eğitim Platformu'na eğitmen olarak kaydınız tamamlandı. " +
                "Aşağıdaki bilgilerle sisteme giriş yapabilirsiniz.",
                details,
                extra,
                baseUrl + "/giris");
        send(toEmail, subject, body);
    }

    private void send(String to, String subject, String htmlBody) {
        if (!mailEnabled) {
            log.debug("Mail devre dışı — gönderilmedi: {} → {}", subject, to);
            return;
        }
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            ClassPathResource logo = new ClassPathResource("static/images/gazi-logo.png");
            if (logo.exists()) {
                helper.addInline("gazi-logo", logo);
            }

            mailSender.send(msg);
        } catch (Exception e) {
            log.error("E-posta gönderilemedi [{}] → {}: {}", subject, to, e.getMessage(), e);
        }
    }
}
