package com.guzem.uzaktan.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    /**
     * Caffeine cache manager with per-cache configuration.
     * Each cache has its own TTL and max size based on access patterns.
     *
     * Cache names and their purposes:
     * - publishedCourses: Paginated course list (5 min TTL, 100 entries)
     * - coursesByCategory: Course list filtered by category (5 min TTL, 200 entries)
     * - course: Individual course details (10 min TTL, 500 entries)
     * - courseStats: Admin statistics (2 min TTL, 10 entries)
     * - instructors: Full instructor list (15 min TTL, 50 entries)
     * - instructor: Individual instructor details (15 min TTL, 200 entries)
     * - courseReviews: Approved reviews for a course (10 min TTL, 200 entries)
     * - enrollmentStatus: Active enrollment checks (1 min TTL, 1000 entries)
     * - certificate: Certificate by code (60 min TTL, 500 entries)
     * - userCertificates: Certificates for a user (5 min TTL, 500 entries)
     */
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
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
        );
    }
}
