package com.guzem.uzaktan.controller.course;

import com.guzem.uzaktan.dto.response.CertificateResponse;
import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.service.common.FileStorageService;
import com.guzem.uzaktan.service.course.CertificateRateLimitService;
import com.guzem.uzaktan.service.course.CertificateService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.file.Path;

@Controller
@RequestMapping("/sertifika")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;
    private final CertificateRateLimitService rateLimitService;
    private final FileStorageService fileStorageService;

    @GetMapping("/dogrula/{code}")
    public String verifyCertificate(@PathVariable String code, Model model, HttpServletRequest request) {
        String clientKey = resolveClientIp(request);
        if (!rateLimitService.tryAcquire(clientKey)) {
            model.addAttribute("errorMessage", "Çok fazla istek gönderdiniz. Lütfen bir dakika sonra tekrar deneyin.");
            return "certificate/verify";
        }
        try {
            model.addAttribute("certificate", certificateService.findByCode(code));
        } catch (ResourceNotFoundException e) {
            model.addAttribute("certificate", null);
        }
        return "certificate/verify";
    }

    @GetMapping("/indir/{code}")
    public ResponseEntity<Resource> download(
            @PathVariable String code,
            @ModelAttribute("currentUserId") Long currentUserId,
            @AuthenticationPrincipal UserDetails principal) {

        CertificateResponse cert;
        try {
            cert = certificateService.findByCode(code);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }

        // Güvenlik: sadece sertifika sahibi veya ADMIN indirebilir
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !cert.getUserId().equals(currentUserId)) {
            return ResponseEntity.status(403).build();
        }

        // PDF henüz üretilmediyse 202 Accepted döndür
        if (cert.getFileUrl() == null) {
            return ResponseEntity.status(202).build();
        }

        Path file = fileStorageService.resolve(cert.getFileUrl());
        Resource resource = new FileSystemResource(file);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"sertifika-" + code + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String clientIp = xff.split(",")[0].trim();
            if (!clientIp.isBlank()) return clientIp;
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) return realIp.trim();
        return request.getRemoteAddr();
    }
}
