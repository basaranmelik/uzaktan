package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.service.course.CourseCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/kategoriler")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CourseCategoryService categoryService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin/categories";
    }

    @GetMapping("/yeni")
    public String newForm() {
        return "admin/category-form";
    }

    @PostMapping
    public String create(@RequestParam String displayName,
                         RedirectAttributes redirectAttributes) {
        try {
            categoryService.create(displayName);
            redirectAttributes.addFlashAttribute("successMessage", "Kategori oluşturuldu.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/kategoriler";
    }

    @PostMapping("/{id}/sil")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Kategori silindi.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/kategoriler";
    }
}
