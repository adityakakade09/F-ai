package com.fraudguard.controller.api;

import com.fraudguard.model.Transaction;
import com.fraudguard.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transaction Management API", description = "Endpoints for query, creation, and audit deletion of financial transactions")
public class ApiTransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping
    @Operation(summary = "Get paginated list of transactions, optionally filtered by account search query")
    public ResponseEntity<Page<Transaction>> getTransactions(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        Page<Transaction> txPage = transactionService.getAllTransactions(
                search, 
                PageRequest.of(page, size, Sort.by("transactionDate").descending())
        );
        return ResponseEntity.ok(txPage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed transaction object by unique transaction ID")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new transaction (runs fraud rules automatically and raises alerts if risky)")
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody Transaction transaction) {
        Transaction savedTx = transactionService.saveTransaction(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTx);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a transaction record from database")
    public ResponseEntity<String> deleteTransaction(@PathVariable("id") Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok("Transaction deleted successfully");
    }
}
