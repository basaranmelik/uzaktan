package com.guzem.uzaktan.controller.user;

import com.guzem.uzaktan.service.user.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/bildirimler")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public String list(@ModelAttribute("currentUserId") Long currentUserId,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        model.addAttribute("notifications", notificationService.getAll(currentUserId, page, 20));
        notificationService.markAllRead(currentUserId);
        return "bildirimler";
    }

    @GetMapping("/sayac")
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Long>> getUnreadCount(
            @ModelAttribute("currentUserId") Long currentUserId) {
        long count = notificationService.getUnreadCount(currentUserId);
        return ResponseEntity.ok(java.util.Map.of("count", count));
    }

    @PostMapping("/{id}/oku")
    @ResponseBody
    public ResponseEntity<Void> markRead(@PathVariable Long id,
                                          @ModelAttribute("currentUserId") Long currentUserId) {
        notificationService.markRead(id, currentUserId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/sil")
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Object>> delete(@PathVariable Long id,
                                                                 @ModelAttribute("currentUserId") Long currentUserId) {
        boolean deleted = notificationService.delete(id, currentUserId);
        if (!deleted) {
            return ResponseEntity.status(404)
                    .body(java.util.Map.of("success", false));
        }
        long unreadCount = notificationService.getUnreadCount(currentUserId);
        return ResponseEntity.ok(java.util.Map.of(
                "success", true,
                "count", unreadCount
        ));
    }
}
