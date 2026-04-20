package com.guzem.uzaktan.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(1000));
        manager.setCacheNames(java.util.List.of(
                "publishedCourses",
                "coursesByCategory",
                "course",
                "courseStats",
                "instructors",
                "instructor",
                "courseReviews",
                "enrollmentStatus",
                "certificate",
                "userCertificates"
        ));
        return manager;
    }
}
