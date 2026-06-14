package com.fraudguard.repository;

import com.fraudguard.model.FraudResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FraudResultRepository extends JpaRepository<FraudResult, Long> {

    Optional<FraudResult> findByTransactionId(Long transactionId);

    Page<FraudResult> findByRiskLevel(String riskLevel, Pageable pageable);

    @Query("SELECT COUNT(f) FROM FraudResult f WHERE f.riskLevel = :riskLevel")
    long countByRiskLevel(String riskLevel);

    @Query("SELECT AVG(f.fraudScore) FROM FraudResult f")
    Double getAverageFraudScore();

    @Query("SELECT f.riskLevel, COUNT(f) FROM FraudResult f GROUP BY f.riskLevel")
    List<Object[]> getRiskDistribution();

    @Query("SELECT f.riskLevel, f.fraudReason, COUNT(f) FROM FraudResult f GROUP BY f.riskLevel, f.fraudReason")
    List<Object[]> getFraudCategories();

    @Query("SELECT f FROM FraudResult f JOIN FETCH f.transaction t WHERE t.accountNumber LIKE %:accountNumber%")
    Page<FraudResult> searchByAccountNumber(String accountNumber, Pageable pageable);
}
