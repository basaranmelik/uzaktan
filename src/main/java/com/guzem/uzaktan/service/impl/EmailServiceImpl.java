package com.guzem.uzaktan.service.impl;

import com.guzem.uzaktan.model.AssignmentSubmission;
import com.guzem.uzaktan.model.User;
import com.guzem.uzaktan.model.ZoomMeeting;
import com.guzem.uzaktan.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    private static final DateTimeFormatter TR_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    // ---------------------------------------------------------------

    @Async
    @Override
    public void sendAssignmentSubmittedToTeacher(User teacher, AssignmentSubmission submission) {
        if (teacher == null || teacher.getEmail() == null) return;
        String subject = "Yeni Ödev Teslimi: " + submission.getAssignment().getTitle();
        String body = section("Yeni Ödev Teslimi",
                "Merhaba " + teacher.getFirstName() + " " + teacher.getLastName() + ",",
                "<b>" + submission.getUser().getFirstName() + " " + submission.getUser().getLastName() + "</b>" +
                " adlı öğrenciniz <b>\"" + submission.getAssignment().getTitle() + "\"</b> ödevini teslim etti.",
                "Kurs: " + submission.getAssignment().getCourse().getTitle(),
                null, null);
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
                : "-";
        String feedbackHtml = submission.getFeedback() != null && !submission.getFeedback().isBlank()
                ? "<p style=\"margin-top:12px;\"><b>Geri bildirim:</b><br>" + escapeHtml(submission.getFeedback()) + "</p>"
                : "";
        String body = section("Ödeviniz Notlandırıldı",
                "Merhaba " + student.getFirstName() + " " + student.getLastName() + ",",
                "<b>\"" + submission.getAssignment().getTitle() + "\"</b> ödeviniz notlandırıldı.",
                "Aldığınız puan: <b>" + scoreText + "</b>",
                feedbackHtml, null);
        send(student.getEmail(), subject, body);
    }

    @Async
    @Override
    public void sendAssignmentDueReminder(User student, String assignmentTitle, String courseTitle, LocalDateTime dueDate) {
        if (student.getEmail() == null) return;
        String subject = "Hatırlatma: \"" + assignmentTitle + "\" ödevi yarın son gün!";
        String body = section("Ödev Son Teslim Günü Yaklaşıyor",
                "Merhaba " + student.getFirstName() + " " + student.getLastName() + ",",
                "<b>\"" + assignmentTitle + "\"</b> ödevinin son teslim tarihi yarına kadar.",
                "Kurs: " + courseTitle + " &nbsp;|&nbsp; Son tarih: <b>" + dueDate.format(TR_FORMAT) + "</b>",
                "<p style=\"color:#e03131;margin-top:8px;\">Henüz teslim yapmadınız. Lütfen ödevinizi zamanında yükleyin.</p>",
                null);
        send(student.getEmail(), subject, body);
    }

    @Async
    @Override
    public void sendMeetingScheduled(User student, ZoomMeeting meeting) {
        if (student.getEmail() == null) return;
        String subject = "Yeni Canlı Ders: " + meeting.getTopic();
        String passwordHtml = meeting.getPassword() != null && !meeting.getPassword().isBlank()
                ? "<p>Toplantı şifresi: <b><code>" + meeting.getPassword() + "</code></b></p>"
                : "";
        String body = section("Yeni Canlı Ders Planlandı",
                "Merhaba " + student.getFirstName() + " " + student.getLastName() + ",",
                "<b>\"" + meeting.getCourse().getTitle() + "\"</b> kursunuza yeni bir canlı ders eklendi.",
                "Konu: <b>" + meeting.getTopic() + "</b><br>" +
                "Tarih: <b>" + meeting.getScheduledAt().format(TR_FORMAT) + "</b><br>" +
                "Süre: <b>" + meeting.getDurationMinutes() + " dakika</b>",
                passwordHtml,
                meeting.getJoinUrl());
        send(student.getEmail(), subject, body);
    }

    @Async
    @Override
    public void sendMeetingCancelled(User student, ZoomMeeting meeting) {
        if (student.getEmail() == null) return;
        String subject = "Canlı Ders İptal Edildi: " + meeting.getTopic();
        String body = section("Canlı Ders İptal Edildi",
                "Merhaba " + student.getFirstName() + " " + student.getLastName() + ",",
                "<b>\"" + meeting.getCourse().getTitle() + "\"</b> kursundaki canlı ders iptal edildi.",
                "İptal edilen ders: <b>" + meeting.getTopic() + "</b><br>" +
                "Planlanan tarih: <b>" + meeting.getScheduledAt().format(TR_FORMAT) + "</b>",
                "<p style=\"color:var(--text-muted);\">Yeni bir ders planlandığında bilgilendirileceksiniz.</p>",
                null);
        send(student.getEmail(), subject, body);
    }

    @Async
    @Override
    public void sendMeetingReminder(User student, ZoomMeeting meeting) {
        if (student.getEmail() == null) return;
        String subject = "30 Dakika Sonra Canlı Ders: " + meeting.getTopic();
        String passwordHtml = meeting.getPassword() != null && !meeting.getPassword().isBlank()
                ? "<p>Toplantı şifresi: <b><code>" + meeting.getPassword() + "</code></b></p>"
                : "";
        String body = section("Canlı Ders 30 Dakika Sonra Başlıyor",
                "Merhaba " + student.getFirstName() + " " + student.getLastName() + ",",
                "<b>\"" + meeting.getTopic() + "\"</b> dersi 30 dakika sonra başlıyor!",
                "Kurs: " + meeting.getCourse().getTitle() + "<br>" +
                "Saat: <b>" + meeting.getScheduledAt().format(TR_FORMAT) + "</b>",
                passwordHtml,
                meeting.getJoinUrl());
        send(student.getEmail(), subject, body);
    }

    // ---------------------------------------------------------------
    // Yardımcı metodlar
    // ---------------------------------------------------------------

    private void send(String to, String subject, String htmlBody) {
        if (!mailEnabled) {
            log.debug("Mail devre dışı — gönderilmedi: {} → {}", subject, to);
            return;
        }
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(msg);
        } catch (Exception e) {
            log.warn("E-posta gönderilemedi [{}] → {}: {}", subject, to, e.getMessage());
        }
    }

    /**
     * Basit HTML e-posta şablonu.
     *
     * @param title       Büyük başlık
     * @param greeting    Selamlama satırı
     * @param mainText    Ana paragraf (HTML olabilir)
     * @param detailText  İkincil detay satırı (HTML olabilir, null ise atlanır)
     * @param extraHtml   Ek HTML bloğu (null ise atlanır)
     * @param joinUrl     Zoom katılım linki (null ise buton gösterilmez)
     */
    private String section(String title, String greeting, String mainText,
                            String detailText, String extraHtml, String joinUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang=\"tr\"><head><meta charset=\"UTF-8\"></head><body style=\"")
          .append("margin:0;padding:0;background:#f1f3f5;font-family:'Segoe UI',Arial,sans-serif;\">");

        sb.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f1f3f5;padding:32px 0;\">")
          .append("<tr><td align=\"center\">");

        sb.append("<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"")
          .append("background:#ffffff;border-radius:12px;overflow:hidden;")
          .append("box-shadow:0 2px 12px rgba(0,0,0,0.08);max-width:600px;\">");

        // Header
        sb.append("<tr><td style=\"background:#1c7ed6;padding:28px 36px;\">")
          .append("<h2 style=\"margin:0;color:#ffffff;font-size:1.25rem;\">")
          .append("🎓 Gazi Üniversitesi — Uzaktan Öğrenme</h2>")
          .append("</td></tr>");

        // Body
        sb.append("<tr><td style=\"padding:32px 36px;\">")
          .append("<h3 style=\"margin:0 0 20px;color:#212529;font-size:1.15rem;\">").append(title).append("</h3>")
          .append("<p style=\"margin:0 0 12px;color:#495057;\">").append(greeting).append("</p>")
          .append("<p style=\"margin:0 0 12px;color:#212529;\">").append(mainText).append("</p>");

        if (detailText != null) {
            sb.append("<div style=\"background:#f8f9fa;border-radius:8px;padding:14px 18px;")
              .append("margin:16px 0;color:#495057;font-size:0.95rem;\">")
              .append(detailText).append("</div>");
        }

        if (extraHtml != null) {
            sb.append(extraHtml);
        }

        if (joinUrl != null) {
            sb.append("<div style=\"margin-top:24px;\">")
              .append("<a href=\"").append(joinUrl).append("\" ")
              .append("style=\"background:#1c7ed6;color:#ffffff;padding:12px 28px;")
              .append("border-radius:8px;text-decoration:none;font-weight:600;font-size:0.95rem;\">")
              .append("Derse Katıl →</a></div>");
        }

        sb.append("</td></tr>");

        // Footer
        sb.append("<tr><td style=\"background:#f8f9fa;padding:18px 36px;")
          .append("border-top:1px solid #dee2e6;color:#868e96;font-size:0.8rem;\">")
          .append("Bu e-posta Gazi Üniversitesi Uzaktan Öğrenme Platformu tarafından otomatik olarak gönderilmiştir. ")
          .append("Lütfen bu e-postayı yanıtlamayın.")
          .append("</td></tr>");

        sb.append("</table></td></tr></table></body></html>");
        return sb.toString();
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                   .replace("\n", "<br>");
    }
}
