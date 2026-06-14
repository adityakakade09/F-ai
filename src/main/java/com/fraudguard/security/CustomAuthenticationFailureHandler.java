package com.fraudguard.security;

import com.fraudguard.model.AuditLog;
import com.fraudguard.model.User;
import com.fraudguard.repository.AuditLogRepository;
import com.fraudguard.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {
        
        String username = request.getParameter("username");
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            userAgent = "Unknown Browser";
        }
        if (userAgent.length() > 150) {
            userAgent = userAgent.substring(0, 150);
        }

        String errorMessage = "Invalid username or password";

        if (username != null && !username.trim().isEmpty()) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.isActive()) {
                    if (!user.isLocked()) {
                        int attempts = user.getFailedLoginAttempts() + 1;
                        user.setFailedLoginAttempts(attempts);
                        
                        if (attempts >= 5) {
                            user.setLocked(true);
                            errorMessage = "Your account has been locked due to 5 consecutive failed login attempts. Please contact admin.";
                            
                            // Audit Lock
                            AuditLog lockAudit = AuditLog.builder()
                                    .username(username)
                                    .action("Account Locked (Too many failed logins)")
                                    .ipAddress(ipAddress)
                                    .browser(userAgent)
                                    .module("SECURITY")
                                    .build();
                            auditLogRepository.save(lockAudit);
                        } else {
                            errorMessage = "Invalid username or password. Attempt " + attempts + " of 5.";
                        }
                        userRepository.save(user);
                    } else {
                        errorMessage = "Your account is locked. Please contact admin.";
                    }
                } else {
                    errorMessage = "Your account is deactivated. Please contact admin.";
                }
            }
        }

        // Save failed login in audit trail
        AuditLog failureAudit = AuditLog.builder()
                .username(username != null ? username : "unknown")
                .action("Web Login Failed: " + exception.getMessage())
                .ipAddress(ipAddress)
                .browser(userAgent)
                .module("SECURITY")
                .build();
        auditLogRepository.save(failureAudit);

        String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        setDefaultFailureUrl("/login?error=true&message=" + encodedMessage);
        super.onAuthenticationFailure(request, response, exception);
    }
}
