package com.guzem.uzaktan.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                buildCache("course", 10, 500),
                buildCache("courseStats", 10, 100),
                buildCache("featuredCourses", 10, 50),
                buildCache("publishedCourses", 5, 200),
                buildCache("coursesByCategory", 5, 200),

                buildCache("enrollmentStatus", 1, 1000),
                buildCache("userCertificates", 5, 500),

                buildCache("certificate", 120, 2000),
                buildCache("courseCategories", 30, 50),

                buildCache("instructorList", 10, 100),
                buildCache("instructorCourses", 10, 200),
                buildCache("courseReviews", 5, 500)
        ));
        return manager;
    }

    private CaffeineCache buildCache(String name, long ttlMinutes, int maxSize) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
                .maximumSize(maxSize)
                .build());
    }
}
