package com.guzem.uzaktan.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@ConfigurationProperties(prefix = "zoom")
@Getter
@Setter
public class ZoomConfig {

    private String accountId;
    private String clientId;
    private String clientSecret;

    @Bean("zoomRestClient")
    public RestClient zoomRestClient() {
        return RestClient.builder()
                .baseUrl("https://api.zoom.us/v2")
                .build();
    }
}
