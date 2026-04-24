package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.service.course.CourseReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/yorumlar")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReviewController {

    private final CourseReviewService courseReviewService;

    @GetMapping
    public String listPendingReviews(Model model) {
        model.addAttribute("pendingReviews", courseReviewService.getPendingReviews());
        return "admin/reviews";
    }

    @PostMapping("/{reviewId}/onayla")
    public String approveReview(@PathVariable Long reviewId, RedirectAttributes redirectAttributes) {
        courseReviewService.approveReview(reviewId);
        redirectAttributes.addFlashAttribute("successMessage", "Yorum onaylandı ve yayına alındı.");
        return "redirect:/admin/yorumlar";
    }

    @PostMapping("/{reviewId}/sil")
    public String deleteReview(@PathVariable Long reviewId, RedirectAttributes redirectAttributes) {
        courseReviewService.deleteReview(reviewId);
        redirectAttributes.addFlashAttribute("successMessage", "Yorum reddedildi / silindi.");
        return "redirect:/admin/yorumlar";
    }
}
