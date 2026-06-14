package com.fraudguard.controller;

import com.fraudguard.model.Transaction;
import com.fraudguard.service.AuditLogService;
import com.fraudguard.service.NotificationService;
import com.fraudguard.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.InputStream;
import java.util.List;

@Controller
@RequestMapping("/datasets")
public class DatasetController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/upload")
    public String uploadPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("username", userDetails.getUsername());
        long unreadNotifications = notificationService.getUnreadCountForUser(userDetails.getUsername());
        model.addAttribute("unreadCount", unreadNotifications);
        return "datasets/upload";
    }

    @PostMapping("/upload")
    public String handleUpload(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam("file") MultipartFile file,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Please select a valid CSV or Excel file to upload.");
            return "redirect:/datasets/upload";
        }

        try (InputStream is = file.getInputStream()) {
            String fileName = file.getOriginalFilename();
            List<Transaction> savedList = transactionService.uploadDataset(is, fileName);
            
            // Audit Log Dataset Upload
            auditLogService.logAction(
                    userDetails.getUsername(),
                    String.format("Uploaded dataset '%s'. Successfully parsed and analyzed %d records.", fileName, savedList.size()),
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent"),
                    "DATASETS"
            );

            redirectAttributes.addFlashAttribute("successMsg", 
                    String.format("Successfully parsed and analyzed %d transactions from '%s'!", savedList.size(), fileName));
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to parse dataset: " + e.getMessage());
            auditLogService.logAction(
                    userDetails.getUsername(),
                    "Failed dataset upload: " + e.getMessage(),
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent"),
                    "DATASETS"
            );
        }

        return "redirect:/datasets/upload";
    }
}
