package com.guzem.uzaktan.service.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.guzem.uzaktan.config.ZoomConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Component
public class ZoomApiClient {

    private final RestClient zoomRestClient;
    private final ZoomConfig zoomConfig;

    // Token cache
    private String cachedToken;
    private Instant tokenExpiry = Instant.EPOCH;

    public ZoomApiClient(@Qualifier("zoomRestClient") RestClient zoomRestClient, ZoomConfig zoomConfig) {
        this.zoomRestClient = zoomRestClient;
        this.zoomConfig = zoomConfig;
    }

    public synchronized String getAccessToken() {
        if (cachedToken != null && Instant.now().isBefore(tokenExpiry)) {
            return cachedToken;
        }

        String credentials = zoomConfig.getClientId() + ":" + zoomConfig.getClientSecret();
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());

        TokenResponse response = RestClient.builder()
                .baseUrl("https://zoom.us")
                .build()
                .post()
                .uri("/oauth/token?grant_type=account_credentials&account_id=" + zoomConfig.getAccountId())
                .header("Authorization", "Basic " + encoded)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .retrieve()
                .body(TokenResponse.class);

        if (response == null || response.getAccessToken() == null) {
            throw new RuntimeException("Zoom token alınamadı.");
        }

        cachedToken = response.getAccessToken();
        // expire 5 dakika erken sayılsın
        tokenExpiry = Instant.now().plusSeconds(response.getExpiresIn() - 300);
        return cachedToken;
    }

    public ZoomApiMeetingResponse createMeeting(ZoomApiMeetingRequest request) {
        return zoomRestClient.post()
                .uri("/users/me/meetings")
                .header("Authorization", "Bearer " + getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(ZoomApiMeetingResponse.class);
    }

    public void updateMeeting(String zoomMeetingId, ZoomApiMeetingRequest request) {
        zoomRestClient.patch()
                .uri("/meetings/" + zoomMeetingId)
                .header("Authorization", "Bearer " + getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    public void deleteMeeting(String zoomMeetingId) {
        zoomRestClient.delete()
                .uri("/meetings/" + zoomMeetingId)
                .header("Authorization", "Bearer " + getAccessToken())
                .retrieve()
                .toBodilessEntity();
    }

    // ---- Inner DTOs ----

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TokenResponse {
        @JsonProperty("access_token")
        private String accessToken;
        @JsonProperty("expires_in")
        private long expiresIn;
    }

    @Getter
    @Setter
    public static class ZoomApiMeetingRequest {
        private String topic;
        private int type = 2; // Scheduled
        @JsonProperty("start_time")
        private String startTime; // ISO-8601: yyyy-MM-dd'T'HH:mm:ss
        private int duration;
        private String timezone = "Europe/Istanbul";
        private Map<String, Object> settings = Map.of(
                "waiting_room", false,
                "join_before_host", false,
                "mute_upon_entry", true
        );
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ZoomApiMeetingResponse {
        private String id;
        @JsonProperty("join_url")
        private String joinUrl;
        @JsonProperty("start_url")
        private String startUrl;
        private String password;
    }
}
