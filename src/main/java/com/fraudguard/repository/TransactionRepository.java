package com.fraudguard.repository;

import com.fraudguard.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByAccountNumberContainingIgnoreCase(String accountNumber, Pageable pageable);

    Page<Transaction> findByTransactionDateBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("SELECT COUNT(t) FROM Transaction t")
    long countTotalTransactions();

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = :status")
    long countByStatus(String status);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.status = :status")
    BigDecimal sumAmountByStatus(String status);

    @Query("SELECT SUM(t.amount) FROM Transaction t")
    BigDecimal sumTotalAmount();

    List<Transaction> findByAccountNumberAndTransactionDateAfter(String accountNumber, LocalDateTime timestamp);

    List<Transaction> findByAccountNumberOrderByTransactionDateDesc(String accountNumber);

    @Query("SELECT t.transactionDate, SUM(t.amount) FROM Transaction t GROUP BY t.transactionDate ORDER BY t.transactionDate ASC")
    List<Object[]> getTransactionVolumeOverTime();
}
