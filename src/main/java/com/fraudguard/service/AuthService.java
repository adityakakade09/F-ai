package com.fraudguard.service;

import com.fraudguard.model.User;

public interface AuthService {
    String login(String username, String password, String ipAddress, String browser);
    User register(User user, String roleName);
    void initiatePasswordReset(String email);
    void resetPassword(String token, String newPassword);
    void initiateEmailVerification(String username);
    void verifyEmail(String token);
    void logout(String token);
}
