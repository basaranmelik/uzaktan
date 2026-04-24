package com.guzem.uzaktan.config.security;

import com.guzem.uzaktan.model.common.Role;
import com.guzem.uzaktan.security.CustomUserDetailsService;
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
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
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
                                "/kullanim", "/gizlilik"
                        ).permitAll()
                        .requestMatchers("/admin/kurslar/*/videolar", "/admin/kurslar/*/videolar/**", "/admin/videolar/**")
                        .hasAnyRole("ADMIN", "TEACHER", "FIRM")
                        .requestMatchers("/admin/kurslar", "/admin/kurslar/**")
                        .hasAnyRole("ADMIN", "FIRM")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/egitmen/**").hasAnyRole("TEACHER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/giris")
                        .loginProcessingUrl("/giris")
                        .usernameParameter("email")
                        .successHandler((req, res, auth) -> {
                            String sonra = req.getParameter("sonra");
                            // Open-Redirect Korumasi
                            if (org.springframework.util.StringUtils.hasText(sonra)
                                    && sonra.startsWith("/")
                                    && !sonra.startsWith("//")
                                    && !sonra.contains("\\")
                                    && !sonra.contains("%")
                                    && !sonra.contains(":")) {
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
                        // TODO: 'unsafe-inline' script-src'den kaldırmak için template'lerdeki
                        //   inline onclick/onchange handler'lar (sss.html, admin/users.html, vb.)
                        //   addEventListener tabanlı yaklaşıma geçirilmelidir.
                        //   Inline JS blokları (izle.html, course-videos.html) zaten dış dosyaya taşındı.
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; " +
                                        "script-src 'self' 'unsafe-inline' cdn.jsdelivr.net cdn.plyr.io; " +
                                        "style-src 'self' 'unsafe-inline' cdn.jsdelivr.net fonts.googleapis.com cdn.plyr.io; " +
                                        "font-src 'self' cdn.jsdelivr.net fonts.gstatic.com; " +
                                        "img-src 'self' data:; " +
                                        "media-src 'self'; " +
                                        "connect-src 'self' cdn.plyr.io; " +
                                        "frame-src https://www.google.com/maps/; " +
                                        "frame-ancestors 'none'"))
                )
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

    private static boolean hasRole(Authentication auth, Role role) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role.getAuthority()));
    }
}
