package com.guzem.uzaktan.controller.course;

import com.guzem.uzaktan.service.course.CertificateRateLimitService;
import com.guzem.uzaktan.service.course.CertificateService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sertifika")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;
    private final CertificateRateLimitService rateLimitService;

    @GetMapping("/dogrula/{code}")
    public String verifyCertificate(@PathVariable String code, Model model, HttpServletRequest request) {
        String clientKey = request.getRemoteAddr();
        if (!rateLimitService.tryAcquire(clientKey)) {
            model.addAttribute("errorMessage", "Çok fazla istek gönderdiniz. Lütfen bir dakika sonra tekrar deneyin.");
            return "certificate/verify";
        }
        model.addAttribute("certificate", certificateService.findByCode(code));
        return "certificate/verify";
    }
}
