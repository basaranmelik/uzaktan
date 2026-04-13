package com.guzem.uzaktan.controller;

import com.guzem.uzaktan.service.InstructorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/egitmenler")
@RequiredArgsConstructor
public class InstructorController {

    private final InstructorService instructorService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("instructors", instructorService.findAll());
        return "egitmenler/liste";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("instructor", instructorService.findById(id));
        return "egitmenler/detay";
    }
}
