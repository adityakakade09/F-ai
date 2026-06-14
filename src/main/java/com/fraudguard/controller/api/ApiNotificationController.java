package com.fraudguard.controller.api;

import com.fraudguard.model.Notification;
import com.fraudguard.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification Alerts API", description = "Endpoints for fetching and updating user alerts")
public class ApiNotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/unread-count")
    @Operation(summary = "Get the active unread notification count for the currently logged-in user")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.ok(0L);
        }
        return ResponseEntity.ok(notificationService.getUnreadCountForUser(userDetails.getUsername()));
    }

    @GetMapping
    @Operation(summary = "Get all notifications targeted to the logged-in user's roles")
    public ResponseEntity<List<Notification>> getNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(notificationService.getNotificationsForUser(userDetails.getUsername()));
    }

    @PostMapping("/read/{id}")
    @Operation(summary = "Mark a specific notification alert as read")
    public ResponseEntity<String> markAsRead(@PathVariable("id") Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok("Notification marked as read");
    }
}
