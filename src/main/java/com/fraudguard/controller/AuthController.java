package com.fraudguard.controller;

import com.fraudguard.model.User;
import com.fraudguard.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "message", required = false) String message,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMsg", message != null ? message : "Invalid username or password.");
        }
        if (logout != null) {
            model.addAttribute("successMsg", "You have been logged out successfully.");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, 
                               @RequestParam("roleName") String roleName,
                               RedirectAttributes redirectAttributes) {
        try {
            authService.register(user, roleName);
            redirectAttributes.addFlashAttribute("successMsg", "Registration successful! A verification link has been logged in the system. Click 'View logs' or complete verification.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Registration failed: " + e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String initiatePasswordReset(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        try {
            authService.initiatePasswordReset(email);
            redirectAttributes.addFlashAttribute("successMsg", "Password reset link has been generated and logged. Check the system log to reset.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Error: " + e.getMessage());
            return "redirect:/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String completePasswordReset(@RequestParam("token") String token,
                                        @RequestParam("password") String password,
                                        RedirectAttributes redirectAttributes) {
        try {
            authService.resetPassword(token, password);
            redirectAttributes.addFlashAttribute("successMsg", "Password reset successfully. You can now login with your new password.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Reset failed: " + e.getMessage());
            return "redirect:/reset-password?token=" + token;
        }
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        try {
            authService.verifyEmail(token);
            redirectAttributes.addFlashAttribute("successMsg", "Your account has been verified and activated! Please login.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Verification failed: " + e.getMessage());
        }
        return "redirect:/login";
    }

    @GetMapping("/403")
    public String accessDenied() {
        return "error/403";
    }
}
