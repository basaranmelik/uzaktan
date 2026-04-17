package com.guzem.uzaktan.controller;

import com.guzem.uzaktan.service.NotificationService;
import com.guzem.uzaktan.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/bildirimler")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails principal,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        model.addAttribute("notifications", notificationService.getAll(userId, page, 20));
        notificationService.markAllRead(userId);
        return "bildirimler";
    }

    @GetMapping("/sayac")
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(java.util.Map.of("count", count));
    }

    @PostMapping("/{id}/oku")
    @ResponseBody
    public ResponseEntity<Void> markRead(@PathVariable Long id,
                                          @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        notificationService.markRead(id, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/sil")
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Object>> delete(@PathVariable Long id,
                                                                 @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        boolean deleted = notificationService.delete(id, userId);
        if (!deleted) {
            return ResponseEntity.status(404)
                    .body(java.util.Map.of("success", false));
        }
        long unreadCount = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(java.util.Map.of(
                "success", true,
                "count", unreadCount
        ));
    }
}
