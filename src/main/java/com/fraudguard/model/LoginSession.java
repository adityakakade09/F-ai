package com.fraudguard.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "LOGIN_SESSIONS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime;

    @Column(name = "logout_time")
    private LocalDateTime logoutTime;

    @Column(name = "ip_address", nullable = false, length = 50)
    private String ipAddress;

    @Column(nullable = false, length = 150)
    private String browser;

    @Column(nullable = false, length = 1000)
    private String token;

    @PrePersist
    protected void onCreate() {
        loginTime = LocalDateTime.now();
    }
}
