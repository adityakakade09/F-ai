package com.fraudguard.security;

import com.fraudguard.model.AuditLog;
import com.fraudguard.model.LoginSession;
import com.fraudguard.repository.AuditLogRepository;
import com.fraudguard.repository.LoginSessionRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    @Autowired
    private LoginSessionRepository loginSessionRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        
        String username = "anonymous";
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            userAgent = "Unknown Browser";
        }
        if (userAgent.length() > 150) {
            userAgent = userAgent.substring(0, 150);
        }

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            username = userDetails.getUsername();
        }

        // Retrieve JWT Token from session
        String jwtToken = (String) request.getSession().getAttribute("jwt_token");
        if (jwtToken != null) {
            Optional<LoginSession> sessionOpt = loginSessionRepository.findByToken(jwtToken);
            if (sessionOpt.isPresent()) {
                LoginSession loginSession = sessionOpt.get();
                loginSession.setLogoutTime(LocalDateTime.now());
                loginSessionRepository.save(loginSession);
            }
        }

        // Audit Log Logout
        AuditLog auditLog = AuditLog.builder()
                .username(username)
                .action("Web Logout Success")
                .ipAddress(ipAddress)
                .browser(userAgent)
                .module("SECURITY")
                .build();
        auditLogRepository.save(auditLog);

        response.sendRedirect("/login?logout=true");
    }
}
