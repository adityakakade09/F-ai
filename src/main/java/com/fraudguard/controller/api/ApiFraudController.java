package com.fraudguard.controller.api;

import com.fraudguard.model.FraudResult;
import com.fraudguard.model.Transaction;
import com.fraudguard.service.FraudDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fraud")
@Tag(name = "Fraud Detection Engine API", description = "Endpoints for dry-run fraud assessments and analytics simulations")
public class ApiFraudController {

    @Autowired
    private FraudDetectionService fraudDetectionService;

    @PostMapping("/analyze")
    @Operation(summary = "Dry-run analyze a transaction payload to get its fraud score, risk category, and rules breakdown without saving it")
    public ResponseEntity<FraudResult> dryRunAnalyze(@RequestBody Transaction transaction) {
        FraudResult result = fraudDetectionService.analyzeTransaction(transaction);
        return ResponseEntity.ok(result);
    }
}
