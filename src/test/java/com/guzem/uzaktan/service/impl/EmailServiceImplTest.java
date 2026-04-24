package com.guzem.uzaktan.service.impl;

import com.guzem.uzaktan.model.admin.Assignment;
import com.guzem.uzaktan.model.admin.AssignmentSubmission;
import com.guzem.uzaktan.model.course.Course;
import com.guzem.uzaktan.model.common.User;
import com.guzem.uzaktan.model.instructor.ZoomMeeting;
import com.guzem.uzaktan.service.impl.common.EmailServiceImpl;
import jakarta.mail.BodyPart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.junit.jupiter.api.BeforeEach;

import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailServiceImpl emailService;

    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(mailSender);
        ReflectionTestUtils.setField(emailService, "fromAddress", "guzem@gazi.edu.tr");
        ReflectionTestUtils.setField(emailService, "mailEnabled", true);

        mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    // ── Yardımcı metodlar ───────────────────────────────────────────

    /**
     * MimeMessage'ın metin içeriğini (HTML) çıkarır.
     * Multipart yapıyı recursive olarak tarar ve text/* content type'ını bulur.
     */
    private String getHtmlContent(MimeMessage message) throws Exception {
        String result = extractTextContent(message);
        return result != null ? result : "";
    }

    private String extractTextContent(Part part) throws Exception {
        String contentType = part.getContentType().toLowerCase();
        Object content = part.getContent();

        if (content instanceof MimeMultipart) {
            MimeMultipart multipart = (MimeMultipart) content;
            // Multipart'ın içinden text içerik bul
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                String result = extractTextContent(bodyPart);
                if (result != null && !result.isEmpty() && !result.contains("\uFFFD")) {
                    return result;
                }
            }
        } else if (contentType.contains("text/")) {
            // Text content
            if (content instanceof String) {
                return (String) content;
            } else if (content instanceof InputStream) {
                return new String(((InputStream) content).readAllBytes(), "UTF-8");
            }
        }
        // Diğer content type'ları (image, attachment vb) yoksay

        return null;
    }

    // ── Yardımcı builder'lar ──────────────────────────────────────

    private User user(String firstName, String lastName, String email) {
        return User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .build();
    }

    private Course course(String title) {
        return Course.builder().title(title).build();
    }

    private Assignment assignment(String title, Course course, int maxScore) {
        return Assignment.builder()
                .title(title)
                .course(course)
                .maxScore(maxScore)
                .build();
    }

    private AssignmentSubmission submission(User student, Assignment assignment) {
        return AssignmentSubmission.builder()
                .user(student)
                .assignment(assignment)
                .build();
    }

    private ZoomMeeting meeting(String topic, Course course, String joinUrl) {
        return ZoomMeeting.builder()
                .topic(topic)
                .course(course)
                .scheduledAt(LocalDateTime.of(2026, 4, 20, 14, 0))
                .durationMinutes(60)
                .joinUrl(joinUrl)
                .build();
    }

    // ── 1. sendAssignmentSubmittedToTeacher ───────────────────────

    @Test
    void sendAssignmentSubmittedToTeacher_mailiEğitmeneGönderir() throws Exception {
        User teacher = user("Ali", "Yılmaz", "ali@gazi.edu.tr");
        User student = user("Ayşe", "Kaya", "ayse@std.gazi.edu.tr");
        Course c = course("Java 101");
        AssignmentSubmission sub = submission(student, assignment("Ödev 1", c, 100));

        emailService.sendAssignmentSubmittedToTeacher(teacher, sub);

        verify(mailSender).send(mimeMessage);
        assertThat(mimeMessage.getAllRecipients()[0].toString()).isEqualTo("ali@gazi.edu.tr");
        assertThat(mimeMessage.getSubject()).contains("Ödev 1");
        String html = getHtmlContent(mimeMessage);
        assertThat(html).contains("Ayşe Kaya");
        assertThat(html).contains("Yeni Ödev Teslimi");
    }

    @Test
    void sendAssignmentSubmittedToTeacher_nullTeacher_mailGönderilmez() {
        emailService.sendAssignmentSubmittedToTeacher(null, new AssignmentSubmission());
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendAssignmentSubmittedToTeacher_emailiNullTeacher_mailGönderilmez() {
        emailService.sendAssignmentSubmittedToTeacher(
                user("Ali", "Yılmaz", null), new AssignmentSubmission());
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    // ── 2. sendAssignmentGradedToStudent ─────────────────────────

    @Test
    void sendAssignmentGradedToStudent_puanVeFeedbackIleMailGönderir() throws Exception {
        User student = user("Mehmet", "Demir", "mehmet@std.gazi.edu.tr");
        AssignmentSubmission sub = submission(student, assignment("Dönem Ödevi", course("Python"), 100));
        sub.setScore(85);
        sub.setFeedback("Güzel çalışma!");

        emailService.sendAssignmentGradedToStudent(sub);

        verify(mailSender).send(mimeMessage);
        assertThat(mimeMessage.getAllRecipients()[0].toString()).isEqualTo("mehmet@std.gazi.edu.tr");
        assertThat(mimeMessage.getSubject()).contains("Dönem Ödevi");
        String html = getHtmlContent(mimeMessage);
        assertThat(html).contains("85");
        assertThat(html).contains("Güzel çalışma");
    }

    @Test
    void sendAssignmentGradedToStudent_puanYoksa_tirelıGosterir() throws Exception {
        User student = user("Zeynep", "Çelik", "zeynep@std.gazi.edu.tr");
        AssignmentSubmission sub = submission(student, assignment("Ödev", course("Kurs"), 50));
        sub.setScore(null);

        emailService.sendAssignmentGradedToStudent(sub);

        verify(mailSender).send(mimeMessage);
        String html = getHtmlContent(mimeMessage);
        assertThat(html).contains("-");
    }

    @Test
    void sendAssignmentGradedToStudent_feedbackHtmlKaçışı() throws Exception {
        User student = user("Can", "Arslan", "can@std.gazi.edu.tr");
        AssignmentSubmission sub = submission(student, assignment("Ödev", course("Kurs"), 100));
        sub.setScore(70);
        sub.setFeedback("<script>alert('xss')</script>");

        emailService.sendAssignmentGradedToStudent(sub);

        String html = getHtmlContent(mimeMessage);
        assertThat(html).doesNotContain("<script>");
        assertThat(html).contains("&lt;script&gt;");
    }

    @Test
    void sendAssignmentGradedToStudent_nullEmail_mailGönderilmez() {
        User student = user("X", "Y", null);
        emailService.sendAssignmentGradedToStudent(
                submission(student, assignment("Ödev", course("Kurs"), 100)));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    // ── 3. sendAssignmentDueReminder ─────────────────────────────

    @Test
    void sendAssignmentDueReminder_doğruİçerikleMailGönderir() throws Exception {
        User student = user("Selin", "Yıldız", "selin@std.gazi.edu.tr");
        LocalDateTime due = LocalDateTime.of(2026, 4, 21, 23, 59);

        emailService.sendAssignmentDueReminder(student, "Bitirme Ödevi", "Yazılım Mühendisliği", due);

        verify(mailSender).send(mimeMessage);
        assertThat(mimeMessage.getAllRecipients()[0].toString()).isEqualTo("selin@std.gazi.edu.tr");
        assertThat(mimeMessage.getSubject()).contains("yarın son gün");
        String html = getHtmlContent(mimeMessage);
        assertThat(html).contains("Bitirme Ödevi");
        assertThat(html).contains("Yazılım Mühendisliği");
        assertThat(html).contains("21.04.2026");
    }

    @Test
    void sendAssignmentDueReminder_nullEmail_mailGönderilmez() {
        emailService.sendAssignmentDueReminder(
                user("X", "Y", null), "Ödev", "Kurs", LocalDateTime.now());
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    // ── 4. sendMeetingScheduled ───────────────────────────────────

    @Test
    void sendMeetingScheduled_şifreLinkVeSüreIleMailGönderir() throws Exception {
        User student = user("Emre", "Şahin", "emre@std.gazi.edu.tr");
        ZoomMeeting m = meeting("Haftalık Ders", course("Veri Yapıları"), "https://zoom.us/j/123");
        m.setPassword("abc123");

        emailService.sendMeetingScheduled(student, m);

        verify(mailSender).send(mimeMessage);
        assertThat(mimeMessage.getAllRecipients()[0].toString()).isEqualTo("emre@std.gazi.edu.tr");
        assertThat(mimeMessage.getSubject()).contains("Haftalık Ders");
        String html = getHtmlContent(mimeMessage);
        assertThat(html).contains("abc123");
        assertThat(html).contains("https://zoom.us/j/123");
        assertThat(html).contains("60 dakika");
    }

    @Test
    void sendMeetingScheduled_şifreYoksa_şifreBölümüOlmaz() throws Exception {
        User student = user("Selin", "Kurt", "selin@std.gazi.edu.tr");
        ZoomMeeting m = meeting("Ders", course("Kurs"), "https://zoom.us/j/456");
        m.setPassword(null);

        emailService.sendMeetingScheduled(student, m);

        String html = getHtmlContent(mimeMessage);
        assertThat(html).doesNotContain("Toplantı şifresi");
    }

    @Test
    void sendMeetingScheduled_nullEmail_mailGönderilmez() {
        emailService.sendMeetingScheduled(
                user("X", "Y", null), meeting("Ders", course("Kurs"), null));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    // ── 5. sendMeetingCancelled ───────────────────────────────────

    @Test
    void sendMeetingCancelled_doğruKonuVeKursAdıylaMail() throws Exception {
        User student = user("Elif", "Çelik", "elif@std.gazi.edu.tr");
        ZoomMeeting m = meeting("İptal Edilen Ders", course("Algoritmalar"), null);

        emailService.sendMeetingCancelled(student, m);

        verify(mailSender).send(mimeMessage);
        assertThat(mimeMessage.getSubject()).contains("İptal Edildi");
        String html = getHtmlContent(mimeMessage);
        assertThat(html).contains("İptal Edilen Ders");
        assertThat(html).contains("Algoritmalar");
    }

    @Test
    void sendMeetingCancelled_nullEmail_mailGönderilmez() {
        emailService.sendMeetingCancelled(
                user("X", "Y", null), meeting("Ders", course("Kurs"), null));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    // ── 6. sendMeetingReminder ────────────────────────────────────

    @Test
    void sendMeetingReminder_30DakikaUyarısıMailGönderir() throws Exception {
        User student = user("Burak", "Aydın", "burak@std.gazi.edu.tr");
        ZoomMeeting m = meeting("Canlı Soru-Cevap", course("Veritabanları"), "https://zoom.us/j/789");
        m.setPassword("pass99");

        emailService.sendMeetingReminder(student, m);

        verify(mailSender).send(mimeMessage);
        assertThat(mimeMessage.getSubject()).contains("30 Dakika");
        String html = getHtmlContent(mimeMessage);
        assertThat(html).contains("Canlı Soru-Cevap");
        assertThat(html).contains("pass99");
        assertThat(html).contains("https://zoom.us/j/789");
    }

    @Test
    void sendMeetingReminder_nullEmail_mailGönderilmez() {
        emailService.sendMeetingReminder(
                user("X", "Y", null), meeting("Ders", course("Kurs"), null));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    // ── 7. mailEnabled = false ────────────────────────────────────

    @Test
    void mailDevreDışıyken_hiçbirMetodMailiGöndermez() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", false);

        User teacher = user("Ali", "Yılmaz", "ali@gazi.edu.tr");
        User student = user("Ayşe", "Kaya", "ayse@std.gazi.edu.tr");
        Course c = course("Kurs");
        AssignmentSubmission sub = submission(student, assignment("Ödev", c, 100));
        sub.setScore(90);
        ZoomMeeting m = meeting("Ders", c, "https://zoom.us/j/1");

        emailService.sendAssignmentSubmittedToTeacher(teacher, sub);
        emailService.sendAssignmentGradedToStudent(sub);
        emailService.sendAssignmentDueReminder(student, "Ödev", "Kurs", LocalDateTime.now());
        emailService.sendMeetingScheduled(student, m);
        emailService.sendMeetingCancelled(student, m);
        emailService.sendMeetingReminder(student, m);

        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    // ── 8. SMTP exception → yutulmalı ────────────────────────────

    @Test
    void smtpHatasında_exceptionDışarıSızmaz() {
        doThrow(new RuntimeException("SMTP bağlantı hatası"))
                .when(mailSender).send(any(MimeMessage.class));

        User student = user("Test", "Kullanıcı", "test@std.gazi.edu.tr");
        ZoomMeeting m = meeting("Ders", course("Kurs"), null);

        assertDoesNotThrow(() -> emailService.sendMeetingScheduled(student, m));
    }

    // ── 9. Gönderici adresi kontrolü ─────────────────────────────

    @Test
    void gondereciAdresiFromFielddenOkunur() throws Exception {
        User student = user("Test", "Kullanıcı", "test@std.gazi.edu.tr");
        ZoomMeeting m = meeting("Ders", course("Kurs"), null);

        emailService.sendMeetingScheduled(student, m);

        assertThat(mimeMessage.getFrom()[0].toString()).contains("guzem@gazi.edu.tr");
    }
}
