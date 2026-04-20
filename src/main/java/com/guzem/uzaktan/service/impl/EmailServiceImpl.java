package com.guzem.uzaktan.service.impl;

import com.guzem.uzaktan.dto.request.ContactRequest;
import com.guzem.uzaktan.model.AssignmentSubmission;
import com.guzem.uzaktan.model.User;
import com.guzem.uzaktan.model.ZoomMeeting;
import com.guzem.uzaktan.service.EmailService;
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

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final DateTimeFormatter TR_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    // ---------------------------------------------------------------

    // ─── Mail Tipleri ────────────────────────────────────────────────

    /** Ödev kategorisi için ikon rengi */
    private static final String COLOR_ASSIGNMENT = "#1a6fad";
    /** Canlı ders kategorisi için ikon rengi */
    private static final String COLOR_ZOOM       = "#0d7a5f";
    /** Uyarı rengi */
    private static final String COLOR_WARNING    = "#c92a2a";

    // ─── Public Email Metodları ──────────────────────────────────────

    @Async
    @Override
    public void sendAssignmentSubmittedToTeacher(User teacher, AssignmentSubmission submission) {
        if (teacher == null || teacher.getEmail() == null) return;
        String subject = "Yeni Ödev Teslimi: " + submission.getAssignment().getTitle();

        String details = row("Öğrenci",
                submission.getUser().getFirstName() + " " + submission.getUser().getLastName()) +
                row("Ödev", submission.getAssignment().getTitle()) +
                row("Kurs", submission.getAssignment().getCourse().getTitle());

        String body = buildEmail(
                "Yeni Ödev Teslimi",
                "📋",
                COLOR_ASSIGNMENT,
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

        String details = row("Ödev", submission.getAssignment().getTitle()) +
                row("Notunuz", scoreText) +
                row("Kurs", submission.getAssignment().getCourse().getTitle());

        String extra = submission.getFeedback() != null && !submission.getFeedback().isBlank()
                ? feedbackBox("Öğretmen Geri Bildirimi", escapeHtml(submission.getFeedback()))
                : null;

        String body = buildEmail(
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

        String details = row("Ödev", assignmentTitle) +
                row("Kurs", courseTitle) +
                row("Son Tarih", "<b style=\"color:#c92a2a;\">" + dueDate.format(TR_FORMAT) + "</b>");

        String extra = alertBox("Henüz ödevinizi teslim etmediniz. Lütfen son tarihe dikkat edin.");

        String body = buildEmail(
                "Ödev Son Teslim Günü Yarın!",
                "⏰",
                COLOR_WARNING,
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

        String details = row("Konu", meeting.getTopic()) +
                row("Kurs", meeting.getCourse().getTitle()) +
                row("Tarih &amp; Saat", meeting.getScheduledAt().format(TR_FORMAT)) +
                row("Süre", meeting.getDurationMinutes() + " dakika") +
                (meeting.getPassword() != null && !meeting.getPassword().isBlank()
                        ? row("Toplantı Şifresi", "<code style=\"background:#f1f3f5;padding:2px 6px;border-radius:4px;font-family:monospace;\">"
                                + meeting.getPassword() + "</code>")
                        : "");

        String body = buildEmail(
                "Yeni Canlı Ders Planlandı",
                "🎥",
                COLOR_ZOOM,
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

        String details = row("İptal Edilen Ders", meeting.getTopic()) +
                row("Kurs", meeting.getCourse().getTitle()) +
                row("Planlanan Tarih", meeting.getScheduledAt().format(TR_FORMAT));

        String body = buildEmail(
                "Canlı Ders İptal Edildi",
                "❌",
                COLOR_WARNING,
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

        String details = row("Konu", meeting.getTopic()) +
                row("Kurs", meeting.getCourse().getTitle()) +
                row("Başlangıç Saati", "<b>" + meeting.getScheduledAt().format(TR_FORMAT) + "</b>") +
                (meeting.getPassword() != null && !meeting.getPassword().isBlank()
                        ? row("Toplantı Şifresi", "<code style=\"background:#f1f3f5;padding:2px 6px;border-radius:4px;font-family:monospace;\">"
                                + meeting.getPassword() + "</code>")
                        : "");

        String body = buildEmail(
                "Ders 30 Dakika Sonra Başlıyor",
                "🔔",
                COLOR_ZOOM,
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
        String subject = "İletişim Formu: " + request.getTopic();
        
        String details = row("Ad Soyad", request.getFirstName() + " " + request.getLastName()) +
                row("E-posta", request.getEmail()) +
                row("Telefon", request.getPhone() != null ? request.getPhone() : "—") +
                row("Konu", request.getTopic()) +
                row("Tarih", LocalDateTime.now().format(TR_FORMAT));

        String body = buildEmail(
                "Yeni İletişim Mesajı",
                "✉️",
                "#113a71",
                "Yeni Bir Mesajınız Var,",
                "Web sitesi üzerindeki iletişim formundan yeni bir başvuru aldınız. Detaylar aşağıdadır:",
                details,
                messageBox("Ziyaretçi Mesajı", escapeHtml(request.getMessage())),
                null);
        
        send(fromAddress, subject, body); // Yöneticiye gönder
    }

    @Async
    @Override
    public void sendCourseAnnouncement(User student, String courseTitle, String subject, String messageText) {
        if (student.getEmail() == null) return;
        
        String details = row("Kurs", courseTitle) +
                row("Tarih", LocalDateTime.now().format(TR_FORMAT));

        String body = buildEmail(
                "Eğitmen Duyurusu",
                "📢",
                "#113a71",
                "Merhaba " + student.getFirstName() + " " + student.getLastName() + ",",
                "<b>" + courseTitle + "</b> kursu için eğitmeniniz tarafından yeni bir duyuru yayınlandı:",
                details,
                messageBox("Duyuru: " + escapeHtml(subject), escapeHtml(messageText).replace("\n", "<br>")),
                null);
                
        send(student.getEmail(), subject, body);
    }

    @Async
    @Override
    public void sendNewAssignmentNotification(User student, com.guzem.uzaktan.model.Assignment assignment) {
        if (student.getEmail() == null) return;
        String subject = "Yeni Ödev Yayınlandı: " + assignment.getTitle();

        String details = row("Ödev", assignment.getTitle()) +
                row("Kurs", assignment.getCourse().getTitle()) +
                row("Son Teslim Tarihi", assignment.getDueDate().format(TR_FORMAT));

        String body = buildEmail(
                "Yeni Ödev Yayınlandı",
                "📝",
                COLOR_ASSIGNMENT,
                "Merhaba " + student.getFirstName() + " " + student.getLastName() + ",",
                "<b>\"" + assignment.getCourse().getTitle() + "\"</b> kursunuzda yeni bir ödev yayınlandı.",
                details,
                null,
                baseUrl + "/panom");
        send(student.getEmail(), subject, body);
    }

    // ─── Gönderim ────────────────────────────────────────────────────

    private void send(String to, String subject, String htmlBody) {
        if (!mailEnabled) {
            log.debug("Mail devre dışı — gönderilmedi: {} → {}", subject, to);
            return;
        }
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            // multipart=true → inline attachment desteği
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            // Gazi logosunu inline olarak göm (cid:gazi-logo)
            ClassPathResource logo = new ClassPathResource("static/images/gazi-logo.png");
            if (logo.exists()) {
                helper.addInline("gazi-logo", logo);
            }

            mailSender.send(msg);
        } catch (Exception e) {
            log.warn("E-posta gönderilemedi [{}] → {}: {}", subject, to, e.getMessage());
        }
    }

    // ─── HTML Şablon Yapılandırıcıları ───────────────────────────────

    /**
     * Ana email template'i. Responsive ve email-client uyumlu tablo tabanlı yapı.
     *
     * @param title      Kart başlığı
     * @param icon       Emoji ikon (Mail istemcilerinde gösterilir)
     * @param accentColor Başlık altı şerit rengi
     * @param greeting   Selamlama
     * @param mainText   Ana mesaj (HTML destekler)
     * @param detailRows {@link #row(String, String)} ile oluşturulan bilgi satırları
     * @param extraHtml  Ek HTML bloğu (null olabilir)
     * @param ctaUrl     CTA butonu linki (null ise gösterilmez)
     */
    private String buildEmail(String title, String icon, String accentColor,
                               String greeting, String mainText,
                               String detailRows, String extraHtml, String ctaUrl) {

        String logoUrl = "cid:gazi-logo";

        return "<!DOCTYPE html>" +
        "<html lang=\"tr\" xmlns=\"http://www.w3.org/1999/xhtml\">" +
        "<head>" +
        "  <meta charset=\"UTF-8\">" +
        "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
        "  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">" +
        "  <title>" + title + "</title>" +
        "</head>" +
        "<body style=\"margin:0;padding:0;background-color:#eef2f7;font-family:'Segoe UI',Helvetica,Arial,sans-serif;-webkit-font-smoothing:antialiased;\">" +

        // Dış wrapper
        "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:#eef2f7;\">" +
        "<tr><td align=\"center\" style=\"padding:40px 16px;\">" +

        // İç kart
        "<table role=\"presentation\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" " +
        "  style=\"max-width:600px;width:100%;background:#ffffff;border-radius:16px;" +
        "  box-shadow:0 4px 24px rgba(11,42,91,0.12);overflow:hidden;\">" +

        // ── Header ──────────────────────────────────────────────────────
        "<tr>" +
        "<td style=\"background:linear-gradient(135deg,#0b2a5b 0%,#113a71 60%,#1a6fad 100%);" +
        "  padding:0;\">" +

        "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">" +
        "<tr>" +
        // Logo
        "<td style=\"padding:24px 32px 20px;\" width=\"64\">" +
        "  <img src=\"" + logoUrl + "\" alt=\"Gazi\" width=\"56\" height=\"56\" " +
        "    style=\"display:block;border-radius:8px;object-fit:contain;background:#fff;padding:6px;\">" +
        "</td>" +
        // Başlık metni
        "<td style=\"padding:24px 32px 20px 0;vertical-align:middle;\">" +
        "  <div style=\"color:#bbe3fa;font-size:11px;font-weight:700;letter-spacing:0.08em;" +
        "    text-transform:uppercase;margin-bottom:4px;\">GAZİ ÜNİVERSİTESİ</div>" +
        "  <div style=\"color:#ffffff;font-size:16px;font-weight:700;line-height:1.3;" +
        "    letter-spacing:-0.01em;\">GUZEM Uzaktan Eğitim ve Araştırma Merkezi</div>" +
        "</td>" +
        "</tr>" +
        "</table>" +

        // Altın şerit
        "<div style=\"height:3px;background:linear-gradient(90deg,#998F4D,#c5b86a,#998F4D);\"></div>" +
        "</td>" +
        "</tr>" +

        // ── Başlık bandı ────────────────────────────────────────────────
        "<tr>" +
        "<td style=\"background:#f8fafc;padding:24px 32px;border-bottom:1px solid #e8edf3;\">" +
        "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">" +
        "<tr>" +
        // İkon kutusu
        "<td width=\"52\" style=\"vertical-align:middle;\">" +
        "  <div style=\"width:44px;height:44px;border-radius:12px;background:" + accentColor + "1a;" +
        "    display:flex;align-items:center;justify-content:center;" +
        "    font-size:22px;line-height:44px;text-align:center;\">" +
        "  " + icon +
        "  </div>" +
        "</td>" +
        // Başlık
        "<td style=\"vertical-align:middle;padding-left:14px;\">" +
        "  <div style=\"font-size:20px;font-weight:700;color:#0b2a5b;line-height:1.3;\">" + title + "</div>" +
        "  <div style=\"width:40px;height:3px;background:" + accentColor + ";border-radius:2px;margin-top:6px;\"></div>" +
        "</td>" +
        "</tr>" +
        "</table>" +
        "</td>" +
        "</tr>" +

        // ── İçerik ──────────────────────────────────────────────────────
        "<tr>" +
        "<td style=\"padding:32px 32px 0;\">" +

        // Selamlama
        "<p style=\"margin:0 0 8px;font-size:15px;color:#495057;font-weight:500;\">" + greeting + "</p>" +

        // Ana metin
        "<p style=\"margin:0 0 24px;font-size:15px;color:#212529;line-height:1.7;\">" + mainText + "</p>" +

        // Detay bilgi tablosu
        (detailRows != null && !detailRows.isBlank() ?
            "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" " +
            "  style=\"background:#f4f7fc;border-radius:12px;border:1px solid #dce6f0;overflow:hidden;margin-bottom:24px;\">" +
            detailRows +
            "</table>"
        : "") +

        // Ekstra HTML
        (extraHtml != null ? extraHtml : "") +

        // CTA Butonu
        (ctaUrl != null ?
            "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin:24px 0;\">" +
            "<tr><td style=\"border-radius:10px;background:" + accentColor + ";\">" +
            "  <a href=\"" + ctaUrl + "\" target=\"_blank\" " +
            "    style=\"display:inline-block;padding:14px 36px;color:#ffffff;font-size:15px;" +
            "    font-weight:700;text-decoration:none;border-radius:10px;letter-spacing:0.02em;" +
            "    font-family:'Segoe UI',Helvetica,Arial,sans-serif;\">" +
            "    🎥&nbsp;&nbsp;Derse Katıl" +
            "  </a>" +
            "</td></tr></table>"
        : "") +

        "</td>" +
        "</tr>" +

        // ── Ayırıcı ─────────────────────────────────────────────────────
        "<tr><td style=\"height:32px;\"></td></tr>" +

        // ── Footer ──────────────────────────────────────────────────────
        "<tr>" +
        "<td style=\"background:#f4f7fc;padding:24px 32px;" +
        "  border-top:2px solid #dce6f0;\">" +

        "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">" +
        "<tr>" +
        "<td>" +
        // Üniversite bilgisi
        "<div style=\"font-size:13px;color:#0b2a5b;font-weight:700;margin-bottom:8px;\">" +
        "  Gazi Üniversitesi &mdash; GUZEM" +
        "</div>" +
        "<div style=\"font-size:12px;color:#6b7280;line-height:1.6;\">" +
        "  Bu e-posta <b>GUZEM Öğrenme Platformu</b> tarafından otomatik olarak gönderilmiştir.<br>" +
        "  Gazi Üniversitesi Uzaktan Eğitim ve Araştırma Merkezi (GUZEM)" +
        "</div>" +
        "</td>" +
        "</tr>" +
        "</table>" +

        "</td>" +
        "</tr>" +

        "</table>" + // iç kart kapanış

        // Platform damgası
        "<p style=\"text-align:center;margin:20px 0 0;font-size:11px;color:#adb5bd;\">" +
        "  &copy; Gazi &Uuml;niversitesi GUZEM &bull; Uzaktan &Ouml;ğrenme Platformu" +
        "</p>" +

        "</td></tr>" +
        "</table>" + // dış wrapper kapanış

        "</body></html>";
    }

    /** Detay satırı — buildEmail'deki tablo içinde kullanılır */
    private String row(String label, String value) {
        return "<tr>" +
               "<td style=\"padding:10px 16px;font-size:13px;font-weight:600;color:#5c7089;" +
               "  white-space:nowrap;border-bottom:1px solid #e8edf3;width:35%;\">" + label + "</td>" +
               "<td style=\"padding:10px 16px;font-size:13px;color:#212529;" +
               "  border-bottom:1px solid #e8edf3;\">" + value + "</td>" +
               "</tr>";
    }

    /** Mesaj kutusu (mavi/soft gri temalı) */
    private String messageBox(String title, String text) {
        return "<div style=\"background:#f8fafc;border-left:4px solid #113a71;border-radius:0 8px 8px 0;" +
               "  padding:14px 18px;margin:0 0 24px;\">" +
               "<div style=\"font-size:12px;font-weight:700;color:#113a71;text-transform:uppercase;" +
               "  letter-spacing:0.06em;margin-bottom:6px;\">" + title + "</div>" +
               "<div style=\"font-size:13px;color:#212529;line-height:1.6;\">" + text + "</div>" +
               "</div>";
    }

    /** Geri bildirim kutusu (yeşil temalı) */
    private String feedbackBox(String title, String text) {
        return "<div style=\"background:#f0faf6;border-left:4px solid #0d7a5f;border-radius:0 8px 8px 0;" +
               "  padding:14px 18px;margin:0 0 24px;\">" +
               "<div style=\"font-size:12px;font-weight:700;color:#0d7a5f;text-transform:uppercase;" +
               "  letter-spacing:0.06em;margin-bottom:6px;\">" + title + "</div>" +
               "<div style=\"font-size:13px;color:#212529;line-height:1.6;\">" + text + "</div>" +
               "</div>";
    }

    /** Uyarı kutusu */
    private String alertBox(String text) {
        return "<div style=\"background:#fff5f5;border-left:4px solid #c92a2a;border-radius:0 8px 8px 0;" +
               "  padding:14px 18px;margin:0 0 24px;\">" +
               "<div style=\"font-size:13px;color:#c92a2a;font-weight:600;\">" + text + "</div>" +
               "</div>";
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                   .replace("\n", "<br>");
    }
}
