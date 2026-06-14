package com.fraudguard.controller;

import com.fraudguard.model.AuditLog;
import com.fraudguard.service.AuditLogService;
import com.fraudguard.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/audit/logs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public String listLogs(@AuthenticationPrincipal UserDetails userDetails,
                           @RequestParam(value = "search", required = false) String search,
                           @RequestParam(value = "page", defaultValue = "0") int page,
                           @RequestParam(value = "size", defaultValue = "10") int size,
                           Model model) {
        Page<AuditLog> auditPage = auditLogService.getAuditLogs(search,
                PageRequest.of(page, size, Sort.by("timestamp").descending()));

        model.addAttribute("logs", auditPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", auditPage.getTotalPages());
        model.addAttribute("search", search);
        model.addAttribute("username", userDetails.getUsername());
        
        long unreadNotifications = notificationService.getUnreadCountForUser(userDetails.getUsername());
        model.addAttribute("unreadCount", unreadNotifications);

        return "audit/index";
    }
}
