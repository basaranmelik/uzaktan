package com.guzem.uzaktan.controller.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guzem.uzaktan.config.instructor.ZoomConfig;
import com.guzem.uzaktan.service.instructor.ZoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ZoomWebhookController {

    private final ZoomConfig zoomConfig;
    private final ZoomService zoomService;
    private final ObjectMapper objectMapper;

    @PostMapping("/webhook/zoom")
    public ResponseEntity<?> handle(
            @RequestBody String rawBody,
            @RequestHeader(value = "x-zoom-signature", required = false) String zoomSignature,
            @RequestHeader(value = "x-zoom-request-timestamp", required = false) String zoomTimestamp,
            @RequestHeader(value = "x-zm-signature", required = false) String zmSignature,
            @RequestHeader(value = "x-zm-request-timestamp", required = false) String zmTimestamp,
            HttpServletRequest request) {

        String signature = zoomSignature != null ? zoomSignature : zmSignature;
        String timestamp = zoomTimestamp != null ? zoomTimestamp : zmTimestamp;

        // URL doğrulama isteğini imzasız da kabul et (proxy/ngrok header kesebilir)
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            String event = root.path("event").asText("");
            log.info("Zoom webhook pre-check: event={}", event);
            if ("endpoint.url_validation".equals(event)) {
                String plainToken = root.path("payload").path("plainToken").asText("");
                if (!plainToken.isBlank()) {
                    String encryptedToken = hmacSha256(zoomConfig.getWebhookSecret(), plainToken);
                    log.info("Zoom URL validation OK — returning encrypted token");
                    return ResponseEntity.ok(Map.of(
                            "plainToken", plainToken,
                            "encryptedToken", encryptedToken));
                }
            }
        } catch (Exception e) {
            log.warn("Zoom webhook pre-check parse error: {}", e.getMessage());
        }

        if (!isSignatureValid(timestamp, rawBody, signature)) {
            log.warn("Zoom webhook: geçersiz imza reddedildi — ts={}, sigLen={}, bodyLen={}",
                    timestamp, signature != null ? signature.length() : 0,
                    rawBody != null ? rawBody.length() : 0);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            JsonNode root = objectMapper.readTree(rawBody);
            String event = root.path("event").asText("");
            log.info("Zoom webhook received: event={}, bodyPreview={}",
                    event, rawBody.length() > 300 ? rawBody.substring(0, 300) + "..." : rawBody);

            if ("recording.completed".equals(event)) {
                String zoomMeetingId = root.path("payload").path("object").path("id").asText("");
                log.info("Zoom webhook: recording.completed — zoomMeetingId={}", zoomMeetingId);
                if (!zoomMeetingId.isBlank()) {
                    zoomService.processRecordingCompleted(zoomMeetingId);
                }
            }

            if ("meeting.started".equals(event)) {
                String zoomMeetingId = root.path("payload").path("object").path("id").asText("");
                log.info("Zoom webhook: meeting.started — zoomMeetingId={}", zoomMeetingId);
                if (!zoomMeetingId.isBlank()) {
                    zoomService.markMeetingAsStarted(zoomMeetingId);
                }
            }

        } catch (Exception e) {
            log.error("Zoom webhook işleme hatası: {}", e.getMessage(), e);
        }

        // Zoom, 2xx dışı yanıt alırsa yeniden gönderir — her zaman 200 dön
        return ResponseEntity.ok().build();
    }

    private boolean isSignatureValid(String timestamp, String body, String signature) {
        if (timestamp == null || body == null || signature == null) return false;
        try {
            // Eski istekleri reddet (5 dakikadan eski)
            long ts = Long.parseLong(timestamp);
            if (Math.abs(Instant.now().getEpochSecond() - ts) > 300) {
                log.warn("Zoom webhook: zaman damgası çok eski, ts={}", ts);
                return false;
            }

            String message = "v0:" + timestamp + ":" + body;
            String expected = "v0=" + hmacSha256(zoomConfig.getWebhookSecret(), message);

            // Constant-time karşılaştırma (timing attack koruması)
            return MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("İmza doğrulama hatası: {}", e.getMessage());
            return false;
        }
    }

    private String hmacSha256(String secret, String message) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }
}
