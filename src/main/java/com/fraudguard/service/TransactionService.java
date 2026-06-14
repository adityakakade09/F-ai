package com.fraudguard.service;

import com.fraudguard.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TransactionService {
    Transaction getTransactionById(Long id);
    Page<Transaction> getAllTransactions(String search, Pageable pageable);
    Transaction saveTransaction(Transaction transaction);
    void deleteTransaction(Long id);
    
    // Dataset Upload parsing
    List<Transaction> uploadDataset(InputStream inputStream, String fileName) throws Exception;
    
    // Stats for Dashboard
    Map<String, Object> getDashboardStats();
}
