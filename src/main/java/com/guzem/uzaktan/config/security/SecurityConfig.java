package com.guzem.uzaktan.config.security;

import com.guzem.uzaktan.model.common.Role;
import com.guzem.uzaktan.security.CustomUserDetails;
import com.guzem.uzaktan.security.CustomUserDetailsService;
import com.guzem.uzaktan.security.PasswordResetFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordResetFilter passwordResetFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/webhook/zoom"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/home",
                                "/egitimler", "/egitimler/{id}",
                                "/egitmenler", "/egitmenler/{id}",
                                "/kayit-ol", "/giris",
                                "/hakkimizda", "/iletisim",
                                "/sertifika/dogrula/**",
                                "/css/**", "/js/**", "/images/**",
                                "/uploads/images/**",
                                "/error/**",
                                "/sss",
                                "/kvkk",
                                "/kullanim", "/gizlilik",
                                "/webhook/zoom",
                        "/hata/**"
                        ).permitAll()
                        .requestMatchers("/admin/kurslar/*/videolar", "/admin/kurslar/*/videolar/**", "/admin/videolar/**")
                        .hasAnyRole("ADMIN", "TEACHER", "FIRM")
                        .requestMatchers("/admin/kurslar", "/admin/kurslar/**")
                        .hasAnyRole("ADMIN", "FIRM")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/egitmen/**").hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers("/sinav/**", "/sepet/**", "/odevlerim/**",
                                "/zoom/derslerim", "/zoom/toplanti/**",
                                "/videolar/**", "/profilim/**", "/kayitlarim",
                                "/panom", "/bildirimler")
                        .authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/giris")
                        .loginProcessingUrl("/giris")
                        .usernameParameter("email")
                        .successHandler((req, res, auth) -> {
                            // Force password change if required
                            if (auth.getPrincipal() instanceof CustomUserDetails details
                                    && details.isPasswordResetRequired()) {
                                res.sendRedirect(req.getContextPath() + "/sifre-degistir");
                                return;
                            }
                            String sonra = req.getParameter("sonra");
                            // Open-Redirect Korumasi: URI parse + host must be absent
                            if (isSafeRedirect(sonra)) {
                                res.sendRedirect(req.getContextPath() + sonra);
                            } else {
                                if (hasRole(auth, Role.ADMIN)) {
                                    res.sendRedirect(req.getContextPath() + "/admin");
                                } else if (hasRole(auth, Role.FIRM)) {
                                    res.sendRedirect(req.getContextPath() + "/admin/kurslar");
                                } else if (hasRole(auth, Role.TEACHER)) {
                                    res.sendRedirect(req.getContextPath() + "/egitmen/panel");
                                } else {
                                    res.sendRedirect(req.getContextPath() + "/panom");
                                }
                            }
                        })
                        .failureUrl("/giris?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/cikis")
                        .logoutSuccessUrl("/?cikis")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .sessionFixation().changeSessionId()
                        .maximumSessions(1)
                        .expiredUrl("/giris?suresi-doldu=true")
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            String path = request.getRequestURI();
                            // Gizli alanlar — yetkisiz kullanıcı 404 görsün
                            if (path.startsWith("/admin") || path.startsWith("/egitmen")) {
                                response.sendRedirect(request.getContextPath() + "/hata/404");
                                return;
                            }
                            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                            String redirect = "/";
                            if (auth != null) {
                                if (hasRole(auth, Role.ADMIN)) redirect = "/admin";
                                else if (hasRole(auth, Role.FIRM)) redirect = "/admin/kurslar";
                                else if (hasRole(auth, Role.TEACHER)) redirect = "/egitmen/panel";
                                else redirect = "/panom";
                            }
                            response.sendRedirect(request.getContextPath() + redirect);
                        })
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .contentTypeOptions(content -> {})
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000))
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; " +
                                        "script-src 'self' cdn.jsdelivr.net cdn.plyr.io; " +
                                        "style-src 'self' 'unsafe-inline' cdn.jsdelivr.net fonts.googleapis.com cdn.plyr.io; " +
                                        "font-src 'self' cdn.jsdelivr.net fonts.gstatic.com; " +
                                        "img-src 'self' data:; " +
                                        "media-src 'self'; " +
                                        "connect-src 'self' cdn.plyr.io; " +
                                        "frame-src https://www.google.com/maps/; " +
                                        "frame-ancestors 'none'"))
                )
                .addFilterAfter(passwordResetFilter, UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    private static boolean isSafeRedirect(String url) {
        if (!org.springframework.util.StringUtils.hasText(url)) return false;
        try {
            java.net.URI uri = new java.net.URI(url).normalize();
            // Must be a relative path — no scheme, no host, no authority
            return uri.getScheme() == null
                    && uri.getHost() == null
                    && uri.getAuthority() == null
                    && uri.getPath().startsWith("/")
                    && !uri.getPath().startsWith("//");
        } catch (java.net.URISyntaxException e) {
            return false;
        }
    }

    private static boolean hasRole(Authentication auth, Role role) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role.getAuthority()));
    }
}
