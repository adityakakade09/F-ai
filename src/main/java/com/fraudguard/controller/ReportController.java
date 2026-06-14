package com.fraudguard.controller;

import com.fraudguard.service.AuditLogService;
import com.fraudguard.service.NotificationService;
import com.fraudguard.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public String index(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("username", userDetails.getUsername());
        long unreadNotifications = notificationService.getUnreadCountForUser(userDetails.getUsername());
        model.addAttribute("unreadCount", unreadNotifications);
        return "reports/index";
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> downloadPDF(@AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request) {
        try {
            byte[] pdf = reportService.generatePDFReport();
            
            // Audit Log
            auditLogService.logAction(
                    userDetails.getUsername(),
                    "Generated and downloaded PDF Audit Report",
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent"),
                    "REPORTS"
            );

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=fraud_report.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> downloadExcel(@AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request) {
        try {
            byte[] excel = reportService.generateExcelReport();
            
            // Audit Log
            auditLogService.logAction(
                    userDetails.getUsername(),
                    "Generated and downloaded Excel Audit Report",
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent"),
                    "REPORTS"
            );

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=fraud_report.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excel);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/csv")
    public ResponseEntity<byte[]> downloadCSV(@AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request) {
        try {
            byte[] csv = reportService.generateCSVReport();
            
            // Audit Log
            auditLogService.logAction(
                    userDetails.getUsername(),
                    "Generated and downloaded CSV Audit Report",
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent"),
                    "REPORTS"
            );

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=fraud_report.csv")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(csv);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
