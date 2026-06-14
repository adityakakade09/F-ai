package com.fraudguard.controller;

import com.fraudguard.model.User;
import com.fraudguard.service.NotificationService;
import com.fraudguard.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class UserManagementController {

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public String listUsers(@AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "size", defaultValue = "10") int size,
                            Model model) {
        Page<User> usersPage = userService.getAllUsers(PageRequest.of(page, size));
        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("username", userDetails.getUsername());
        
        long unreadNotifications = notificationService.getUnreadCountForUser(userDetails.getUsername());
        model.addAttribute("unreadCount", unreadNotifications);
        
        return "users/index";
    }

    @PostMapping("/status/{id}")
    public String toggleUserStatus(@PathVariable("id") Long id, 
                                   @RequestParam("active") boolean active, 
                                   RedirectAttributes redirectAttributes) {
        try {
            userService.setUserActiveStatus(id, active);
            redirectAttributes.addFlashAttribute("successMsg", "User active status updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to update status: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/lock/{id}")
    public String toggleUserLock(@PathVariable("id") Long id, 
                                 @RequestParam("locked") boolean locked, 
                                 RedirectAttributes redirectAttributes) {
        try {
            userService.setUserLockStatus(id, locked);
            redirectAttributes.addFlashAttribute("successMsg", locked ? "User account locked successfully." : "User account unlocked successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to update lock: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMsg", "User account deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to delete user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
