package com.fraudguard.service.impl;

import com.fraudguard.model.FraudResult;
import com.fraudguard.model.Transaction;
import com.fraudguard.repository.TransactionRepository;
import com.fraudguard.service.FraudDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class FraudDetectionServiceImpl implements FraudDetectionService {

    @Autowired
    private TransactionRepository transactionRepository;

    // A static set of blacklisted accounts for evaluation
    private static final Set<String> BLACKLISTED_ACCOUNTS = Set.of(
            "ACC-90823412", "ACC-BAD-01", "ACC-BAD-02", "ACC-SPAM-99"
    );

    @Override
    @Transactional
    public FraudResult analyzeTransaction(Transaction transaction) {
        double score = 0.0;
        List<String> reasons = new ArrayList<>();

        // 1. Blacklisted Account Check (40 pts)
        if (BLACKLISTED_ACCOUNTS.contains(transaction.getAccountNumber()) || 
            transaction.getAccountNumber().startsWith("ACC-BLACK")) {
            score += 40.0;
            reasons.add("Blacklisted account number");
        }

        // 2. Large Transaction Amount Check (15 or 30 pts)
        BigDecimal amount = transaction.getAmount();
        if (amount.compareTo(new BigDecimal("50000.00")) > 0) {
            score += 30.0;
            reasons.add("Critical amount threshold exceeded: $" + amount);
        } else if (amount.compareTo(new BigDecimal("10000.00")) > 0) {
            score += 15.0;
            reasons.add("Large transaction amount detected: $" + amount);
        }

        // Fetch recent transactions for the same account to run context rules
        List<Transaction> recentTransactions = transactionRepository
                .findByAccountNumberOrderByTransactionDateDesc(transaction.getAccountNumber());

        // Filter out the current transaction itself if it's already saved
        List<Transaction> historicalTxs = recentTransactions.stream()
                .filter(t -> transaction.getId() == null || !t.getId().equals(transaction.getId()))
                .toList();

        // 3. Multiple Transactions in Short Time (20 pts)
        LocalDateTime now = transaction.getTransactionDate();
        LocalDateTime tenMinsAgo = now.minusMinutes(10);
        
        long recentCountInTenMins = historicalTxs.stream()
                .filter(t -> t.getTransactionDate().isAfter(tenMinsAgo))
                .count();

        if (recentCountInTenMins >= 3) {
            score += 20.0;
            reasons.add("Multiple transactions in a short time frame (" + recentCountInTenMins + " in 10 mins)");
        }

        // 4. Geographic Location Mismatch (25 pts)
        if (!historicalTxs.isEmpty()) {
            Transaction lastTx = historicalTxs.get(0);
            if (!lastTx.getLocation().equalsIgnoreCase(transaction.getLocation())) {
                long durationMins = Duration.between(lastTx.getTransactionDate(), now).toMinutes();
                // If transactions happen in different locations in less than 2 hours
                if (Math.abs(durationMins) < 120) {
                    score += 25.0;
                    reasons.add(String.format("Geographic location mismatch (last location: '%s' %d mins ago vs current location: '%s')",
                            lastTx.getLocation(), durationMins, transaction.getLocation()));
                }
            }
        }

        // 5. Suspicious Transaction Frequency (15 pts)
        LocalDateTime twentyFourHoursAgo = now.minusHours(24);
        long countIn24Hours = historicalTxs.stream()
                .filter(t -> t.getTransactionDate().isAfter(twentyFourHoursAgo))
                .count();

        if (countIn24Hours >= 8) {
            score += 15.0;
            reasons.add("High daily transaction frequency (" + countIn24Hours + " in 24 hours)");
        }

        // 6. Unusual User Behaviour (10 pts)
        int hour = now.getHour();
        if (hour >= 0 && hour <= 4) {
            score += 10.0;
            reasons.add("Transaction occurred at unusual hours (" + hour + " AM)");
        }

        // 7. Device Mismatch (15 pts)
        if (!historicalTxs.isEmpty()) {
            Transaction lastTx = historicalTxs.get(0);
            if (!lastTx.getDevice().equalsIgnoreCase(transaction.getDevice())) {
                score += 15.0;
                reasons.add(String.format("Device mismatch (last device: '%s' vs current device: '%s')",
                        lastTx.getDevice(), transaction.getDevice()));
            }
        }

        // 8. Rapid Transaction Attempts / Card Testing (20 pts)
        // Check if there are 2+ small transactions (< $10) in last 5 mins, and current is larger
        if (amount.compareTo(new BigDecimal("50.00")) > 0) {
            LocalDateTime fiveMinsAgo = now.minusMinutes(5);
            long smallTxCount = historicalTxs.stream()
                    .filter(t -> t.getTransactionDate().isAfter(fiveMinsAgo))
                    .filter(t -> t.getAmount().compareTo(new BigDecimal("10.00")) < 0)
                    .count();

            if (smallTxCount >= 2) {
                score += 20.0;
                reasons.add("Rapid transaction attempts pattern detected (possible card testing with " + smallTxCount + " small transactions)");
            }
        }

        // Capping score between 0 and 100
        score = Math.max(0.0, Math.min(100.0, score));

        // Determine Risk Category
        String riskLevel;
        if (score < 30) {
            riskLevel = "LOW";
        } else if (score < 60) {
            riskLevel = "MEDIUM";
        } else if (score < 85) {
            riskLevel = "HIGH";
        } else {
            riskLevel = "CRITICAL";
        }

        // Join explanations or provide default
        String explanation = reasons.isEmpty() ? 
                "Transaction pattern normal. Device and location match historical profile." : 
                String.join("; ", reasons);

        return FraudResult.builder()
                .transaction(transaction)
                .fraudScore(score)
                .riskLevel(riskLevel)
                .fraudReason(explanation)
                .build();
    }
}
