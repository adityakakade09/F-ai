package com.fraudguard.controller;

import com.fraudguard.model.Notification;
import com.fraudguard.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public String listNotifications(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        List<Notification> list = notificationService.getNotificationsForUser(userDetails.getUsername());
        model.addAttribute("notifications", list);
        model.addAttribute("username", userDetails.getUsername());
        
        long unreadNotifications = notificationService.getUnreadCountForUser(userDetails.getUsername());
        model.addAttribute("unreadCount", unreadNotifications);
        
        return "notifications/index";
    }

    @PostMapping("/read/{id}")
    public String markRead(@PathVariable("id") Long id) {
        notificationService.markAsRead(id);
        return "redirect:/notifications";
    }

    @PostMapping("/read-all")
    public String markAllRead(@AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAllAsReadForUser(userDetails.getUsername());
        return "redirect:/notifications";
    }
}
