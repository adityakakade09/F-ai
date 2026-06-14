package com.fraudguard.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICATIONS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(name = "read_status", nullable = false)
    @Builder.Default
    private boolean readStatus = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "target_role", nullable = false, length = 50)
    private String targetRole; // ROLE_SUPER_ADMIN, ROLE_FRAUD_ANALYST, ROLE_AUDITOR, ALL

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
