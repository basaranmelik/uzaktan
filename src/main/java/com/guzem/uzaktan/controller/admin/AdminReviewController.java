package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.dto.response.ActionResult;
import com.guzem.uzaktan.service.course.CourseReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
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
    public ActionResult approveReview(@PathVariable Long reviewId) {
        try {
            courseReviewService.approveReview(reviewId);
            return ActionResult.success("Yorum onaylandı.", "/admin/yorumlar");
        } catch (Exception e) {
            log.error("Yorum onaylama hatası: {}", e.getMessage(), e);
            return ActionResult.error(e.getMessage());
        }
    }

    @PostMapping("/{reviewId}/sil")
    public ActionResult deleteReview(@PathVariable Long reviewId) {
        try {
            courseReviewService.deleteReview(reviewId);
            return ActionResult.success("Yorum silindi.", "/admin/yorumlar");
        } catch (Exception e) {
            log.error("Yorum silme hatası: {}", e.getMessage(), e);
            return ActionResult.error(e.getMessage());
        }
    }
}
