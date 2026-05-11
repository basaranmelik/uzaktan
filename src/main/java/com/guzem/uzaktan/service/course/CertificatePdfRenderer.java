package com.guzem.uzaktan.service.course;

import com.guzem.uzaktan.model.course.Certificate;
import com.guzem.uzaktan.repository.course.CertificateRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

/**
 * Orijinal PDF sertifika şablonu üzerine placeholder'ları overlay eder.
 * Tasarım birebir korunur — PDF native fontları kullanılmaz,
 * placeholder üstü kapatılıp yeni metin yazılır.
 */
@Slf4j
@Service
public class CertificatePdfRenderer {

    private final CertificateRepository certificateRepository;

    @Value("${app.upload.dir:${user.home}/guzem-uploads}")
    private String uploadDir;

    private byte[] templateBytes;

    public CertificatePdfRenderer(CertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }

    @PostConstruct
    private void init() {
        try (InputStream is = new ClassPathResource("certificate/template.pdf").getInputStream()) {
            templateBytes = is.readAllBytes();
            log.info("Sertifika PDF şablonu yüklendi: {} bytes", templateBytes.length);
        } catch (Exception e) {
            log.error("Sertifika PDF şablonu yüklenemedi!", e);
        }
    }

    @Async("taskExecutor")
    @Transactional
    public void generateAndSave(Long certificateId) {
        try {
            Certificate cert = certificateRepository.findById(certificateId).orElse(null);
            if (cert == null) {
                log.warn("Sertifika bulunamadı: {}", certificateId);
                return;
            }

            String userName = cert.getUser().getFirstName() + " " + cert.getUser().getLastName();
            String certCode = cert.getCertificateCode();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            String issueDate = cert.getIssueDate().format(fmt);

            try (PDDocument doc = Loader.loadPDF(templateBytes)) {
                PDPage page0 = doc.getPage(0);
                PDPage page1 = doc.getPage(1);

                // Sayfa 1: İsim
                overlayText(doc, page0, userName, 395, 497, 18, Color.decode("#134075"),
                        Standard14Fonts.FontName.HELVETICA_BOLD);
                // Sayfa 2: İsim
                overlayText(doc, page1, userName, 316, 441, 11, Color.decode("#231F20"),
                        Standard14Fonts.FontName.HELVETICA);
                // Sayfa 2: TC Kimlik
                overlayText(doc, page1, "—", 316, 390, 11, Color.decode("#231F20"),
                        Standard14Fonts.FontName.HELVETICA);
                // Sayfa 2: Tarih
                overlayText(doc, page1, issueDate, 316, 338, 11, Color.decode("#231F20"),
                        Standard14Fonts.FontName.HELVETICA);
                // Sayfa 2: Kod
                overlayText(doc, page1, certCode, 316, 298, 11, Color.decode("#231F20"),
                        Standard14Fonts.FontName.HELVETICA);

                Path dir = Paths.get(uploadDir, "sertifikalar");
                Files.createDirectories(dir);
                Path target = dir.resolve(certCode + ".pdf");
                doc.save(target.toFile());

                cert.setFileUrl("sertifikalar/" + certCode + ".pdf");
                certificateRepository.save(cert);
                log.info("Sertifika PDF (PDFBox overlay) oluşturuldu: {}", certCode);
            }

        } catch (Exception e) {
            log.error("Sertifika PDF render hatası (id={}): {}", certificateId, e.getMessage(), e);
        }
    }

    private void overlayText(PDDocument doc, PDPage page, String text, float x, float y, int fontSize,
                             Color color, Standard14Fonts.FontName fontName) throws Exception {
        float pdfY = page.getMediaBox().getHeight() - y;

        try (PDPageContentStream cs = new PDPageContentStream(
                doc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {

            float textWidth = (text.length() * fontSize * 0.6f) + 20;
            cs.setNonStrokingColor(Color.WHITE);
            cs.addRect(x - 5, pdfY - fontSize - 2, textWidth, fontSize + 6);
            cs.fill();

            cs.beginText();
            cs.setFont(new PDType1Font(fontName), fontSize);
            cs.setNonStrokingColor(color);
            cs.newLineAtOffset(x, pdfY - fontSize);
            cs.showText(text);
            cs.endText();
        }
    }

    // Test için public helper
    public static byte[] renderTest(String userName, String issueDate, String certCode) throws Exception {
        try (InputStream is = new ClassPathResource("certificate/template.pdf").getInputStream()) {
            byte[] templateBytes = is.readAllBytes();
            try (PDDocument doc = Loader.loadPDF(templateBytes)) {
                PDPage page1 = doc.getPage(1);
                CertificatePdfRenderer r = new CertificatePdfRenderer(null);

                r.overlayText(doc, page1, userName, 316, 441, 11, Color.decode("#231F20"),
                        Standard14Fonts.FontName.HELVETICA);
                r.overlayText(doc, page1, issueDate, 316, 338, 11, Color.decode("#231F20"),
                        Standard14Fonts.FontName.HELVETICA);
                r.overlayText(doc, page1, certCode, 316, 298, 11, Color.decode("#231F20"),
                        Standard14Fonts.FontName.HELVETICA);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                doc.save(baos);
                return baos.toByteArray();
            }
        }
    }
}
