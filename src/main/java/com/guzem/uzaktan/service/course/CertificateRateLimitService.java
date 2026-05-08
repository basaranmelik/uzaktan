package com.guzem.uzaktan.service.course;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Sertifika doğrulama endpoint'leri için token-bucket tabanlı rate limiter.
 * Her istemci IP'si için dakikada sabit sayıda istek hakkı tanır.
 * Caffeine cache ile sınırlı boyut ve otomatik TTL sağlanır (bellek sızıntısı önlenir).
 */
@Service
public class CertificateRateLimitService {

    private static final int REQUESTS_PER_MINUTE = 10;

    private final Cache<String, Bucket> cache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build();

    public boolean tryAcquire(String clientKey) {
        Bucket bucket = cache.get(clientKey, k -> Bucket.builder()
                .addLimit(limit -> limit.capacity(REQUESTS_PER_MINUTE)
                        .refillIntervally(REQUESTS_PER_MINUTE, Duration.ofMinutes(1)))
                .build());
        return bucket.tryConsume(1);
    }
}
