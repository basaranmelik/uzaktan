package com.guzem.uzaktan.service.impl.course;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Orijinal PDF şablon üzerine text overlay (PDFBox).
 *
 * <pre>
 * ./mvnw test -Dtest=CertificatePdfRenderTest -pl .
 * open target/test-output/sertifika-overlay.pdf
 * </pre>
 */
class CertificatePdfRenderTest {

    @Test
    void shouldOverlayTextOnPdf() throws Exception {
        String userName = "Ahmet YILMAZ";
        String certCode = "OVERLAY-TEST-001";
        String issueDate = "10.05.2026";

        byte[] templateBytes;
        try (InputStream is = new ClassPathResource("certificate/template.pdf").getInputStream()) {
            templateBytes = is.readAllBytes();
        }

        try (PDDocument doc = Loader.loadPDF(templateBytes)) {
            PDPage p0 = doc.getPage(0);
            PDPage p1 = doc.getPage(1);

            float pageH = p0.getMediaBox().getHeight();

            // Sayfa 1: İsim (ortalanmış, büyük)
            overlay(doc, p0, userName, 395, 497, 18, Color.decode("#134075"),
                    Standard14Fonts.FontName.HELVETICA_BOLD);

            // Sayfa 2: placeholder'lar
            overlay(doc, p1, userName,     316, 441, 11, Color.decode("#231F20"),
                    Standard14Fonts.FontName.HELVETICA);
            overlay(doc, p1, "12345678901", 316, 390, 11, Color.decode("#231F20"),
                    Standard14Fonts.FontName.HELVETICA);
            overlay(doc, p1, issueDate,    316, 338, 11, Color.decode("#231F20"),
                    Standard14Fonts.FontName.HELVETICA);
            overlay(doc, p1, certCode,     316, 298, 11, Color.decode("#231F20"),
                    Standard14Fonts.FontName.HELVETICA);

            Path dir = Path.of("target", "test-output");
            Files.createDirectories(dir);
            Path pdfFile = dir.resolve("sertifika-overlay.pdf");
            doc.save(pdfFile.toFile());

            assertThat(pdfFile).exists();
            assertThat(Files.size(pdfFile)).isGreaterThan(10000);
            System.out.println("\n============================================");
            System.out.println("  PDFBox overlay PDF: " + pdfFile.toAbsolutePath());
            System.out.println("  Boyut: " + Files.size(pdfFile) + " bytes");
            System.out.println("============================================\n");
        }
    }

    private void overlay(PDDocument doc, PDPage page, String text, float x, float bottom,
                         int fontSize, Color color, Standard14Fonts.FontName fontName) throws Exception {
        float y = page.getMediaBox().getHeight() - bottom;

        try (PDPageContentStream cs = new PDPageContentStream(
                doc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {

            float w = text.length() * fontSize * 0.6f + 20;
            cs.setNonStrokingColor(Color.WHITE);
            cs.addRect(x - 5, y - fontSize - 2, w, fontSize + 6);
            cs.fill();

            cs.beginText();
            cs.setFont(new PDType1Font(fontName), fontSize);
            cs.setNonStrokingColor(color);
            cs.newLineAtOffset(x, y - fontSize);
            cs.showText(text);
            cs.endText();
        }
    }
}
