package com.guzem.uzaktan.service.user;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class PasswordChangeRateLimitService {

    private static final int MAX_ATTEMPTS = 5;

    private final Cache<String, Bucket> cache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .build();

    public boolean tryConsume(String userId) {
        Bucket bucket = cache.get(userId, k -> Bucket.builder()
                .addLimit(limit -> limit.capacity(MAX_ATTEMPTS)
                        .refillIntervally(MAX_ATTEMPTS, Duration.ofMinutes(15)))
                .build());
        return bucket.tryConsume(1);
    }
}
