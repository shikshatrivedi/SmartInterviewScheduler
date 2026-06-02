package com.shiksha.scheduler.controller;

import com.shiksha.scheduler.model.User;
import com.shiksha.scheduler.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired private NotificationService notificationService;

    /** Mark a single notification as read (AJAX call) */
    @PostMapping("/read/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> markRead(@PathVariable Long id,
                                                         @AuthenticationPrincipal User user) {
        notificationService.markRead(id);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    /** Mark all notifications as read */
    @PostMapping("/read-all")
    @ResponseBody
    public ResponseEntity<Map<String, String>> markAllRead(@AuthenticationPrincipal User user) {
        notificationService.markAllRead(user);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    /** Get unread count (used for badge update via AJAX) */
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> getCount(@AuthenticationPrincipal User user) {
        long count = (user != null) ? notificationService.countUnread(user) : 0L;
        return ResponseEntity.ok(Map.of("count", count));
    }
}
