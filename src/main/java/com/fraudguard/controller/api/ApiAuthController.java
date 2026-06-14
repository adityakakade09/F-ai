package com.fraudguard.controller.api;

import com.fraudguard.dto.AuthResponse;
import com.fraudguard.dto.LoginRequest;
import com.fraudguard.dto.RegistrationRequest;
import com.fraudguard.model.User;
import com.fraudguard.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication API", description = "Endpoints for user authentication, registration, and session management")
public class ApiAuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and get JWT Bearer Token")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        String token = authService.login(
                loginRequest.getUsername(),
                loginRequest.getPassword(),
                request.getRemoteAddr(),
                request.getHeader("User-Agent")
        );
        return ResponseEntity.ok(AuthResponse.builder().token(token).build());
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account (defaults to inactive until verified)")
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegistrationRequest regRequest) {
        User user = User.builder()
                .username(regRequest.getUsername())
                .password(regRequest.getPassword())
                .email(regRequest.getEmail())
                .fullName(regRequest.getFullName())
                .build();
        
        authService.register(user, regRequest.getRole());
        return ResponseEntity.ok("Registration successful! Please verify your email using the link logged in system console/audit trail.");
    }

    @PostMapping("/logout")
    @Operation(summary = "Log out active JWT session")
    public ResponseEntity<String> logoutUser(@RequestHeader("Authorization") String tokenHeader) {
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            String token = tokenHeader.substring(7);
            authService.logout(token);
            return ResponseEntity.ok("Logged out successfully");
        }
        return ResponseEntity.badRequest().body("Invalid Authorization Header");
    }
}
