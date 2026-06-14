package com.fraudguard.controller;

import com.fraudguard.model.FraudResult;
import com.fraudguard.model.Transaction;
import com.fraudguard.repository.FraudResultRepository;
import com.fraudguard.service.NotificationService;
import com.fraudguard.service.TransactionService;
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
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private FraudResultRepository fraudResultRepository;

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public String listTransactions(@AuthenticationPrincipal UserDetails userDetails,
                                   @RequestParam(value = "search", required = false) String search,
                                   @RequestParam(value = "page", defaultValue = "0") int page,
                                   @RequestParam(value = "size", defaultValue = "10") int size,
                                   Model model) {
        Page<Transaction> txPage = transactionService.getAllTransactions(search, 
                PageRequest.of(page, size, Sort.by("transactionDate").descending()));
        
        model.addAttribute("transactions", txPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", txPage.getTotalPages());
        model.addAttribute("search", search);
        model.addAttribute("username", userDetails.getUsername());
        
        long unreadNotifications = notificationService.getUnreadCountForUser(userDetails.getUsername());
        model.addAttribute("unreadCount", unreadNotifications);
        
        return "transactions/index";
    }

    @GetMapping("/{id}")
    public String viewDetails(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable("id") Long id, 
                              Model model) {
        Transaction tx = transactionService.getTransactionById(id);
        FraudResult res = fraudResultRepository.findByTransactionId(id).orElse(null);
        
        model.addAttribute("transaction", tx);
        model.addAttribute("fraudResult", res);
        model.addAttribute("username", userDetails.getUsername());
        
        long unreadNotifications = notificationService.getUnreadCountForUser(userDetails.getUsername());
        model.addAttribute("unreadCount", unreadNotifications);
        
        return "transactions/details";
    }
}
