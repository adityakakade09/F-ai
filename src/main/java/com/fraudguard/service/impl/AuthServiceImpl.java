package com.fraudguard.service.impl;

import com.fraudguard.model.*;
import com.fraudguard.repository.*;
import com.fraudguard.security.JwtTokenProvider;
import com.fraudguard.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Autowired
    private LoginSessionRepository loginSessionRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Override
    @Transactional
    public String login(String username, String password, String ipAddress, String browser) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!user.isActive()) {
            throw new IllegalStateException("Your account is deactivated. Please contact admin.");
        }

        if (user.isLocked()) {
            throw new IllegalStateException("Your account is locked due to too many failed attempts. Please contact admin.");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            // Increment failed attempts
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= 5) {
                user.setLocked(true);
                // Log Lock event
                auditLogRepository.save(AuditLog.builder()
                        .username(username)
                        .action("Account Locked (Too many failed logins)")
                        .ipAddress(ipAddress)
                        .browser(browser)
                        .module("SECURITY")
                        .build());
            }
            userRepository.save(user);
            throw new IllegalArgumentException("Invalid username or password");
        }

        // Reset failed attempts on success
        if (user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }

        // Generate JWT
        String token = tokenProvider.generateToken(username);

        // Record Login Session
        LoginSession session = LoginSession.builder()
                .username(username)
                .loginTime(LocalDateTime.now())
                .ipAddress(ipAddress)
                .browser(browser)
                .token(token)
                .build();
        loginSessionRepository.save(session);

        // Log successful login
        auditLogRepository.save(AuditLog.builder()
                .username(username)
                .action("REST Login Success")
                .ipAddress(ipAddress)
                .browser(browser)
                .module("SECURITY")
                .build());

        return token;
    }

    @Override
    @Transactional
    public User register(User user, String roleName) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(false); // Inactive until email is verified
        user.setLocked(false);
        user.setFailedLoginAttempts(0);

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findByName(roleName).ifPresent(roles::add);
        if (roles.isEmpty()) {
            roleRepository.findByName("ROLE_FRAUD_ANALYST").ifPresent(roles::add);
        }
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        // Initiate email verification
        initiateEmailVerification(savedUser.getUsername());

        return savedUser;
    }

    @Override
    @Transactional
    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No account registered with this email: " + email));

        // Delete existing reset tokens for this user
        passwordResetTokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();

        passwordResetTokenRepository.save(resetToken);

        // Log the link in Audit Logs for testing
        String resetLink = "/reset-password?token=" + token;
        System.out.println("=== PASSWORD RESET LINK: " + resetLink);
        
        auditLogRepository.save(AuditLog.builder()
                .username(user.getUsername())
                .action("Password Reset Initiated. Link generated: " + resetLink)
                .ipAddress("127.0.0.1")
                .browser("System Server")
                .module("SECURITY")
                .build());
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired password reset token"));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new IllegalArgumentException("Password reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);

        // Audit Success
        auditLogRepository.save(AuditLog.builder()
                .username(user.getUsername())
                .action("Password Reset Successfully Completed")
                .ipAddress("127.0.0.1")
                .browser("System Server")
                .module("SECURITY")
                .build());
    }

    @Override
    @Transactional
    public void initiateEmailVerification(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Delete existing verification tokens
        emailVerificationTokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();

        emailVerificationTokenRepository.save(verificationToken);

        // Log link in Audit log
        String verifyLink = "/verify-email?token=" + token;
        System.out.println("=== EMAIL VERIFICATION LINK: " + verifyLink);

        auditLogRepository.save(AuditLog.builder()
                .username(user.getUsername())
                .action("Email Verification Link generated: " + verifyLink)
                .ipAddress("127.0.0.1")
                .browser("System Server")
                .module("SECURITY")
                .build());
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification token"));

        if (verificationToken.isExpired()) {
            emailVerificationTokenRepository.delete(verificationToken);
            throw new IllegalArgumentException("Email verification token has expired");
        }

        User user = verificationToken.getUser();
        user.setActive(true); // Activate account
        userRepository.save(user);

        emailVerificationTokenRepository.delete(verificationToken);

        // Audit verification
        auditLogRepository.save(AuditLog.builder()
                .username(user.getUsername())
                .action("Email Verified. Account Activated.")
                .ipAddress("127.0.0.1")
                .browser("System Server")
                .module("SECURITY")
                .build());
    }

    @Override
    @Transactional
    public void logout(String token) {
        Optional<LoginSession> sessionOpt = loginSessionRepository.findByToken(token);
        if (sessionOpt.isPresent()) {
            LoginSession session = sessionOpt.get();
            session.setLogoutTime(LocalDateTime.now());
            loginSessionRepository.save(session);

            // Audit logout
            auditLogRepository.save(AuditLog.builder()
                    .username(session.getUsername())
                    .action("REST Logout Success")
                    .ipAddress(session.getIpAddress())
                    .browser(session.getBrowser())
                    .module("SECURITY")
                    .build());
        }
    }
}
