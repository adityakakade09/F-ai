package com.fraudguard.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "FRAUD_RESULTS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false, unique = true)
    private Transaction transaction;

    @Column(name = "fraud_score", nullable = false, precision = 5, scale = 2)
    private double fraudScore;

    @Column(name = "risk_level", nullable = false, length = 50)
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "fraud_reason", nullable = false, length = 1000)
    private String fraudReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
