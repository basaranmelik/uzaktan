package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.dto.request.InstructorCreateRequest;
import com.guzem.uzaktan.dto.request.InstructorUpdateRequest;
import com.guzem.uzaktan.service.instructor.InstructorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/egitmenler")
@RequiredArgsConstructor
public class AdminInstructorController {

    private final InstructorService instructorService;

    @GetMapping
    public String listInstructors(Model model) {
        model.addAttribute("instructors", instructorService.findAll());
        return "admin/instructors";
    }

    @GetMapping("/yeni")
    public String createForm(Model model) {
        model.addAttribute("instructorRequest", new InstructorCreateRequest());
        return "admin/instructor-form";
    }

    @PostMapping
    public String createInstructor(@Valid @ModelAttribute("instructorRequest") InstructorCreateRequest request,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/instructor-form";
        }
        instructorService.create(request);
        redirectAttributes.addFlashAttribute("successMessage", "Eğitmen başarıyla eklendi.");
        return "redirect:/admin/egitmenler";
    }

    @GetMapping("/{id}/duzenle")
    public String editForm(@PathVariable Long id, Model model) {
        var instructor = instructorService.findById(id);
        
        InstructorUpdateRequest request = new InstructorUpdateRequest();
        request.setName(instructor.getName());
        request.setExpertise(instructor.getExpertise());

        model.addAttribute("instructorRequest", request);
        model.addAttribute("instructorId", id);
        model.addAttribute("photoUrl", instructor.getPhotoUrl());
        return "admin/instructor-form";
    }

    @PostMapping("/{id}")
    public String updateInstructor(@PathVariable Long id,
                                   @Valid @ModelAttribute("instructorRequest") InstructorUpdateRequest request,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        if (result.hasErrors()) {
            model.addAttribute("instructorId", id);
            return "admin/instructor-form";
        }
        instructorService.update(id, request);
        redirectAttributes.addFlashAttribute("successMessage", "Eğitmen başarıyla güncellendi.");
        return "redirect:/admin/egitmenler";
    }

    @PostMapping("/{id}/sil")
    public String deleteInstructor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        instructorService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Eğitmen silindi.");
        return "redirect:/admin/egitmenler";
    }
}
