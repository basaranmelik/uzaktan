package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.dto.response.ActionResult;
import com.guzem.uzaktan.service.course.CourseCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
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
    public ActionResult create(@RequestParam String displayName) {
        try {
            categoryService.create(displayName);
            return ActionResult.success("Kategori oluşturuldu.", "/admin/kategoriler");
        } catch (IllegalArgumentException e) {
            return ActionResult.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/sil")
    public ActionResult delete(@PathVariable Long id) {
        try {
            categoryService.delete(id);
            return ActionResult.success("Kategori silindi.", "/admin/kategoriler");
        } catch (Exception e) {
            log.error("Kategori silinirken hata: {}", e.getMessage(), e);
            return ActionResult.error(e.getMessage());
        }
    }
}
