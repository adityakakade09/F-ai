package com.fraudguard.security;

import com.fraudguard.model.AuditLog;
import com.fraudguard.model.LoginSession;
import com.fraudguard.model.User;
import com.fraudguard.repository.AuditLogRepository;
import com.fraudguard.repository.LoginSessionRepository;
import com.fraudguard.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private LoginSessionRepository loginSessionRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        // 1. Reset failed attempts
        if (user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }

        // 2. Generate a token for tracking the session
        String jwtToken = tokenProvider.generateToken(user.getUsername());
        request.getSession().setAttribute("jwt_token", jwtToken);

        // 3. Save session in database
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            userAgent = "Unknown Browser";
        }
        if (userAgent.length() > 150) {
            userAgent = userAgent.substring(0, 150);
        }

        LoginSession loginSession = LoginSession.builder()
                .username(user.getUsername())
                .loginTime(LocalDateTime.now())
                .ipAddress(ipAddress)
                .browser(userAgent)
                .token(jwtToken)
                .build();
        loginSessionRepository.save(loginSession);

        // 4. Create Audit Log
        AuditLog auditLog = AuditLog.builder()
                .username(user.getUsername())
                .action("Web Login Success")
                .ipAddress(ipAddress)
                .browser(userAgent)
                .module("SECURITY")
                .build();
        auditLogRepository.save(auditLog);

        setDefaultTargetUrl("/dashboard");
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
