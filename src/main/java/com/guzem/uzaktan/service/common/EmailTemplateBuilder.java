package com.guzem.uzaktan.service.common;

import org.springframework.stereotype.Component;

/**
 * Builds HTML email templates. Outlook, Gmail, Apple Mail, Thunderbird compatible table-based layout.
 */
@Component
public class EmailTemplateBuilder {

    private static final String COLOR_ASSIGNMENT = "#1a6fad";
    private static final String COLOR_ZOOM       = "#0d7a5f";
    private static final String COLOR_WARNING    = "#c92a2a";

    public String getColorAssignment() { return COLOR_ASSIGNMENT; }
    public String getColorZoom()       { return COLOR_ZOOM; }
    public String getColorWarning()    { return COLOR_WARNING; }

    /**
     * Main email template.
     *
     * @param title       Card title
     * @param icon        Emoji icon
     * @param accentColor Header accent color
     * @param greeting    Greeting line
     * @param mainText    Main message (HTML supported)
     * @param detailRows  Info rows built with {@link #row} and {@link #rowFirst}
     * @param extraHtml   Extra HTML block (nullable)
     * @param ctaUrl      CTA button link (nullable)
     */
    public String buildEmail(String title, String icon, String accentColor,
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
        "  <!--[if mso]><noscript><xml><o:OfficeDocumentSettings><o:PixelsPerInch>96</o:PixelsPerInch></o:OfficeDocumentSettings></xml></noscript><![endif]-->" +
        "</head>" +
        "<body style=\"margin:0;padding:0;background-color:#eef2f7;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Helvetica,Arial,sans-serif;-webkit-font-smoothing:antialiased;-moz-osx-font-smoothing:grayscale;\">" +

        "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" bgcolor=\"#eef2f7\" style=\"background-color:#eef2f7;\">" +
        "<tr><td align=\"center\" style=\"padding:40px 16px 32px;\">" +

        "<!--[if mso]><table role=\"presentation\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td><![endif]-->" +
        "<table role=\"presentation\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" " +
        "  style=\"max-width:600px;width:100%;background:#ffffff;border-radius:12px;" +
        "  border:1px solid #dce6f0;box-shadow:0 2px 16px rgba(11,42,91,0.08);\">" +

        "<tr>" +
        "<td bgcolor=\"#0b2a5b\" style=\"background:#0b2a5b;background:linear-gradient(135deg,#0b2a5b 0%,#113a71 60%,#1a6fad 100%);padding:0;border-radius:12px 12px 0 0;\">" +

        "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">" +
        "<tr>" +
        "<td width=\"76\" style=\"padding:24px 0 20px 32px;vertical-align:middle;\">" +
        "  <img src=\"" + logoUrl + "\" alt=\"G\" width=\"52\" height=\"52\" " +
        "    style=\"display:block;border-radius:6px;background:#fff;padding:4px;\">" +
        "</td>" +
        "<td style=\"padding:24px 32px 20px 12px;vertical-align:middle;\">" +
        "  <div style=\"color:#9dc8eb;font-size:11px;font-weight:700;letter-spacing:0.08em;" +
        "    text-transform:uppercase;margin:0 0 4px;font-family:Arial,Helvetica,sans-serif;\">GAZİ ÜNİVERSİTESİ</div>" +
        "  <div style=\"color:#ffffff;font-size:15px;font-weight:700;line-height:1.35;margin:0;" +
        "    letter-spacing:-0.01em;\">GUZEM<br>Uzaktan Eğitim ve Araştırma Merkezi</div>" +
        "</td>" +
        "</tr>" +
        "</table>" +

        "</td></tr>" +
        "<tr>" +
        "<td bgcolor=\"#998F4D\" style=\"background-color:#998F4D;height:3px;line-height:3px;font-size:0;\"" +
        "  height=\"3\">" +
        "  &nbsp;" +
        "</td>" +
        "</tr>" +

        "<tr>" +
        "<td bgcolor=\"#f8fafc\" style=\"background-color:#f8fafc;padding:24px 32px;border-bottom:1px solid #e8edf3;\">" +
        "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">" +
        "<tr>" +
        "<td width=\"52\" style=\"vertical-align:middle;\">" +
        "  <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">" +
        "  <tr><td width=\"44\" height=\"44\" style=\"width:44px;height:44px;border-radius:10px;" +
        "    background-color:" + accentColor + ";text-align:center;vertical-align:middle;" +
        "    font-size:22px;line-height:1;\">" +
        "    " + icon +
        "  </td></tr></table>" +
        "</td>" +
        "<td style=\"vertical-align:middle;padding-left:14px;\">" +
        "  <div style=\"font-size:20px;font-weight:700;color:#0b2a5b;line-height:1.3;margin:0;\">" + title + "</div>" +
        "  <div style=\"width:40px;height:3px;background:" + accentColor + ";border-radius:2px;margin-top:6px;font-size:0;line-height:0;\">&nbsp;</div>" +
        "</td>" +
        "</tr>" +
        "</table>" +
        "</td>" +
        "</tr>" +

        "<tr>" +
        "<td style=\"padding:32px 32px 0;\">" +

        "<p style=\"margin:0 0 8px;font-size:15px;color:#495057;font-weight:500;\">" + greeting + "</p>" +

        "<p style=\"margin:0 0 24px;font-size:15px;color:#212529;line-height:1.65;\">" + mainText + "</p>" +

        (detailRows != null && !detailRows.isBlank() ?
            "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" " +
            "  style=\"background:#f4f7fc;border-radius:10px;border:1px solid #dce6f0;margin-bottom:24px;\">" +
            detailRows +
            "</table>"
        : "") +

        (extraHtml != null ? extraHtml : "") +

        (ctaUrl != null ?
            "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin:24px 0 28px;\">" +
            "<tr><td bgcolor=\"" + accentColor + "\" style=\"background-color:" + accentColor + ";" +
            "  border-radius:8px;text-align:center;\">" +
            "  <a href=\"" + ctaUrl + "\" target=\"_blank\" " +
            "    style=\"display:inline-block;padding:14px 36px;color:#ffffff;font-size:15px;" +
            "    font-weight:700;text-decoration:none;border-radius:8px;letter-spacing:0.02em;" +
            "    font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Helvetica,Arial,sans-serif;" +
            "    mso-padding-alt:0;\">" +
            "    <span style=\"mso-text-raise:15px;\">" + (icon.contains("\uD83C\uDFA5") || icon.contains("\uD83D\uDD14") ? "\uD83C\uDFA5&nbsp;&nbsp;Derse Katıl" : "Panele Git") + "</span>" +
            "  </a>" +
            "</td></tr></table>"
        : "") +

        "</td>" +
        "</tr>" +

        "<tr>" +
        "<td bgcolor=\"#f4f7fc\" style=\"background-color:#f4f7fc;padding:24px 32px;" +
        "  border-top:2px solid #dce6f0;border-radius:0 0 12px 12px;\">" +

        "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">" +
        "<tr>" +
        "<td style=\"vertical-align:top;\">" +
        "<div style=\"font-size:13px;color:#0b2a5b;font-weight:700;margin:0 0 6px;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Arial,sans-serif;\">" +
        "  Gazi Üniversitesi &mdash; GUZEM" +
        "</div>" +
        "<div style=\"font-size:12px;color:#6b7280;line-height:1.6;margin:0;\">" +
        "  Bu e-posta <b>GUZEM Öğrenme Platformu</b> tarafından<br>" +
        "  otomatik olarak gönderilmiştir.<br>" +
        "  Gazi Üniversitesi Uzaktan Eğitim ve Araştırma Merkezi" +
        "</div>" +
        "</td>" +
        "</tr>" +
        "</table>" +

        "</td>" +
        "</tr>" +

        "</table>" +
        "<!--[if mso]></td></tr></table><![endif]-->" +

        "<p style=\"text-align:center;margin:20px 0 0;font-size:11px;color:#adb5bd;" +
        "  font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Helvetica,Arial,sans-serif;\">" +
        "  &copy; Gazi &Uuml;niversitesi GUZEM &bull; Uzaktan &Ouml;ğrenme Platformu" +
        "</p>" +

        "</td></tr>" +
        "</table>" +

        "</body></html>";
    }

    public String row(String label, String value) {
        return "<tr>" +
               "<td style=\"padding:10px 16px;font-size:13px;font-weight:600;color:#5c7089;" +
               "  white-space:nowrap;border-top:1px solid #e8edf3;width:35%;" +
               "  font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Helvetica,Arial,sans-serif;\">" + escapeHtmlBasic(label) + "</td>" +
               "<td style=\"padding:10px 16px;font-size:13px;color:#212529;" +
               "  border-top:1px solid #e8edf3;" +
               "  font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Helvetica,Arial,sans-serif;\">" + escapeHtmlBasic(value) + "</td>" +
               "</tr>";
    }

    public String rowFirst(String label, String value) {
        return "<tr>" +
               "<td style=\"padding:10px 16px;font-size:13px;font-weight:600;color:#5c7089;" +
               "  white-space:nowrap;width:35%;" +
               "  font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Helvetica,Arial,sans-serif;\">" + escapeHtmlBasic(label) + "</td>" +
               "<td style=\"padding:10px 16px;font-size:13px;color:#212529;" +
               "  font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Helvetica,Arial,sans-serif;\">" + escapeHtmlBasic(value) + "</td>" +
               "</tr>";
    }

    public String messageBox(String title, String text) {
        return "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" " +
               "  style=\"margin:0 0 24px;border-left:4px solid #113a71;background:#f8fafc;\">" +
               "<tr><td style=\"padding:14px 18px;border-radius:0 6px 6px 0;\">" +
               "<p style=\"margin:0 0 6px;font-size:12px;font-weight:700;color:#113a71;" +
               "  text-transform:uppercase;letter-spacing:0.06em;" +
               "  font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Helvetica,Arial,sans-serif;\">" + title + "</p>" +
               "<p style=\"margin:0;font-size:13px;color:#212529;line-height:1.6;\">" + text + "</p>" +
               "</td></tr></table>";
    }

    public String feedbackBox(String title, String text) {
        return "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" " +
               "  style=\"margin:0 0 24px;border-left:4px solid #0d7a5f;background:#ecfdf5;\">" +
               "<tr><td style=\"padding:14px 18px;\">" +
               "<p style=\"margin:0 0 6px;font-size:12px;font-weight:700;color:#0d7a5f;" +
               "  text-transform:uppercase;letter-spacing:0.06em;" +
               "  font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Helvetica,Arial,sans-serif;\">" + title + "</p>" +
               "<p style=\"margin:0;font-size:13px;color:#212529;line-height:1.6;\">" + text + "</p>" +
               "</td></tr></table>";
    }

    public String alertBox(String text) {
        return "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" " +
               "  style=\"margin:0 0 24px;border-left:4px solid #c92a2a;background:#fef2f2;\">" +
               "<tr><td style=\"padding:14px 18px;\">" +
               "<p style=\"margin:0;font-size:13px;color:#c92a2a;font-weight:600;" +
               "  font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Helvetica,Arial,sans-serif;\">" + text + "</p>" +
               "</td></tr></table>";
    }

    public String escapeHtmlBasic(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }

    public String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("\n", "<br>");
    }
}
