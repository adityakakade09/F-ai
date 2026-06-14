package com.fraudguard.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "AUDIT_LOGS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String action;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "ip_address", nullable = false, length = 50)
    private String ipAddress;

    @Column(nullable = false, length = 150)
    private String browser;

    @Column(nullable = false, length = 100)
    private String module; // SECURITY, TRANSACTIONS, DATASETS, USERS, REPORTS, SYSTEM

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
