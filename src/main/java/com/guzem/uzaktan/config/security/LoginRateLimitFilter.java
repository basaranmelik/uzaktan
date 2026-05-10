package com.guzem.uzaktan.config.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class LoginRateLimitFilter extends OncePerRequestFilter {

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    private Bucket resolveBucket(String ip) {
        return buckets.get(ip, key -> {
            Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(5)));
            return Bucket.builder().addLimit(limit).build();
        });
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (request.getMethod().equalsIgnoreCase("POST") && request.getRequestURI().equals("/giris")) {
            String ip = request.getRemoteAddr();
            Bucket bucket = resolveBucket(ip);
            if (!bucket.tryConsume(1)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write("""
                        <!DOCTYPE html><html lang="tr"><head><meta charset="UTF-8"><title>Çok fazla deneme</title></head>
                        <body style="display:flex;align-items:center;justify-content:center;min-height:100vh;
                        font-family:sans-serif;text-align:center;background:#f8f9fa;">
                        <div><h1 style="color:#c92a2a;">429 — Çok fazla deneme</h1>
                        <p>5 dakika içinde en fazla 5 giriş denemesi yapabilirsiniz. Lütfen daha sonra tekrar deneyin.</p>
                        <a href="/giris" style="color:#1c7ed6;">Giriş sayfasına dön</a></div></body></html>""");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
