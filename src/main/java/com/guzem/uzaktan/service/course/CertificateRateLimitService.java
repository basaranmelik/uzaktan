package com.guzem.uzaktan.service.course;

import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sertifika doğrulama endpoint'leri için token-bucket tabanlı rate limiter.
 * Her istemci IP'si (veya key) için dakikada sabit sayıda istek hakkı tanır.
 *
 * Thread-safe {@link ConcurrentHashMap} + immutable {@link Bucket} yapısı
 * ile ekstra cache/sunucu bağımlılığı olmadan çalışır.
 */
@Service
public class CertificateRateLimitService {

    private static final int REQUESTS_PER_MINUTE = 10;

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public boolean tryAcquire(String clientKey) {
        Bucket bucket = cache.computeIfAbsent(clientKey, k -> Bucket.builder()
                .addLimit(limit -> limit.capacity(REQUESTS_PER_MINUTE)
                        .refillIntervally(REQUESTS_PER_MINUTE, Duration.ofMinutes(1)))
                .build());
        return bucket.tryConsume(1);
    }
}
