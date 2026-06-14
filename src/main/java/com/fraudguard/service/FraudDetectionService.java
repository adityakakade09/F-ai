package com.fraudguard.service;

import com.fraudguard.model.FraudResult;
import com.fraudguard.model.Transaction;

public interface FraudDetectionService {
    FraudResult analyzeTransaction(Transaction transaction);
}
