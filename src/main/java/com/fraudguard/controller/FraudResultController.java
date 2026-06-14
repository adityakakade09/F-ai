package com.fraudguard.controller;

import com.fraudguard.model.FraudResult;
import com.fraudguard.repository.FraudResultRepository;
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
@RequestMapping("/fraud")
public class FraudResultController {

    @Autowired
    private FraudResultRepository fraudResultRepository;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/cases")
    public String listCases(@AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam(value = "risk", required = false) String risk,
                            @RequestParam(value = "search", required = false) String search,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "size", defaultValue = "10") int size,
                            Model model) {
        Page<FraudResult> fraudPage;
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());

        if (search != null && !search.trim().isEmpty()) {
            fraudPage = fraudResultRepository.searchByAccountNumber(search, pageRequest);
        } else if (risk != null && !risk.trim().isEmpty() && !risk.equalsIgnoreCase("ALL")) {
            fraudPage = fraudResultRepository.findByRiskLevel(risk.toUpperCase(), pageRequest);
        } else {
            fraudPage = fraudResultRepository.findAll(pageRequest);
        }

        model.addAttribute("cases", fraudPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", fraudPage.getTotalPages());
        model.addAttribute("risk", risk);
        model.addAttribute("search", search);
        model.addAttribute("username", userDetails.getUsername());
        
        long unreadNotifications = notificationService.getUnreadCountForUser(userDetails.getUsername());
        model.addAttribute("unreadCount", unreadNotifications);

        return "fraud/index";
    }

    @GetMapping("/cases/{id}")
    public String viewDetails(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable("id") Long id,
                              Model model) {
        FraudResult res = fraudResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fraud case not found with ID: " + id));
        
        model.addAttribute("case", res);
        model.addAttribute("username", userDetails.getUsername());
        
        long unreadNotifications = notificationService.getUnreadCountForUser(userDetails.getUsername());
        model.addAttribute("unreadCount", unreadNotifications);
        
        return "fraud/details";
    }
}
