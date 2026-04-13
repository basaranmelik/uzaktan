package com.guzem.uzaktan.controller;

import com.guzem.uzaktan.service.CertificateService;
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

    @GetMapping("/dogrula/{code}")
    public String verifyCertificate(@PathVariable String code, Model model) {
        model.addAttribute("certificate", certificateService.findByCode(code));
        return "certificate/verify";
    }
}
