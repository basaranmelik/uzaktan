package com.guzem.uzaktan.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class PasswordResetFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails details) {
            if (details.isPasswordResetRequired()) {
                String path = request.getRequestURI();
                if (!isAllowedPath(path)) {
                    response.sendRedirect(request.getContextPath() + "/sifre-degistir");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowedPath(String path) {
        return path.equals("/sifre-degistir")
                || path.equals("/cikis")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/uploads/images/");
    }
}
