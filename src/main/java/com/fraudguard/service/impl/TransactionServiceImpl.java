package com.fraudguard.service.impl;

import com.fraudguard.model.FraudResult;
import com.fraudguard.model.Transaction;
import com.fraudguard.repository.FraudResultRepository;
import com.fraudguard.repository.TransactionRepository;
import com.fraudguard.repository.UserRepository;
import com.fraudguard.service.FraudDetectionService;
import com.fraudguard.service.NotificationService;
import com.fraudguard.service.TransactionService;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private FraudResultRepository fraudResultRepository;

    @Autowired
    private FraudDetectionService fraudDetectionService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Transaction> getAllTransactions(String search, Pageable pageable) {
        if (search != null && !search.trim().isEmpty()) {
            return transactionRepository.findByAccountNumberContainingIgnoreCase(search, pageable);
        }
        return transactionRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Transaction saveTransaction(Transaction transaction) {
        // Save transaction
        Transaction savedTx = transactionRepository.save(transaction);
        
        // Analyze and save fraud result
        FraudResult result = fraudDetectionService.analyzeTransaction(savedTx);
        fraudResultRepository.save(result);
        
        // Trigger notification if High/Critical risk
        if ("HIGH".equals(result.getRiskLevel()) || "CRITICAL".equals(result.getRiskLevel())) {
            String alertMsg = String.format("Alert: %s risk transaction detected! Acc: %s, Amt: $%s. Reason: %s",
                    result.getRiskLevel(), savedTx.getAccountNumber(), savedTx.getAmount(), result.getFraudReason());
            notificationService.createNotification(alertMsg, "ROLE_FRAUD_ANALYST");
            notificationService.createNotification(alertMsg, "ROLE_SUPER_ADMIN");
        }
        
        return savedTx;
    }

    @Override
    @Transactional
    public void deleteTransaction(Long id) {
        Transaction tx = getTransactionById(id);
        transactionRepository.delete(tx);
    }

    @Override
    @Transactional
    public List<Transaction> uploadDataset(InputStream inputStream, String fileName) throws Exception {
        List<Transaction> transactionsToSave = new ArrayList<>();
        
        if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
            transactionsToSave = parseExcel(inputStream);
        } else if (fileName.endsWith(".csv")) {
            transactionsToSave = parseCSV(inputStream);
        } else {
            throw new IllegalArgumentException("Unsupported file format. Please upload a CSV or Excel file.");
        }

        if (transactionsToSave.isEmpty()) {
            throw new IllegalArgumentException("The uploaded file contains no valid data rows.");
        }

        List<Transaction> savedTransactions = new ArrayList<>();
        for (Transaction tx : transactionsToSave) {
            savedTransactions.add(saveTransaction(tx));
        }

        return savedTransactions;
    }

    private List<Transaction> parseExcel(InputStream inputStream) throws Exception {
        List<Transaction> txList = new ArrayList<>();
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        
        int rowCount = sheet.getPhysicalNumberOfRows();
        if (rowCount <= 1) {
            throw new IllegalArgumentException("Excel sheet is empty or contains only header.");
        }

        // Validate Headers: expect: Account Number, Amount, Location, Device
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new IllegalArgumentException("Invalid Excel file: missing header row.");
        }

        // Read Rows
        for (int i = 1; i < rowCount; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String accNum = getCellValueAsString(row.getCell(0));
            String amtStr = getCellValueAsString(row.getCell(1));
            String loc = getCellValueAsString(row.getCell(2));
            String dev = getCellValueAsString(row.getCell(3));

            if (accNum.isEmpty() || amtStr.isEmpty() || loc.isEmpty() || dev.isEmpty()) {
                continue; // Skip incomplete records
            }

            BigDecimal amount;
            try {
                amount = new BigDecimal(amtStr.replace(",", ""));
                if (amount.compareTo(BigDecimal.ZERO) <= 0) continue; // Skip zero/negative amounts
            } catch (NumberFormatException e) {
                continue; // Skip invalid numbers
            }

            Transaction tx = Transaction.builder()
                    .accountNumber(accNum)
                    .amount(amount)
                    .location(loc)
                    .device(dev)
                    .status("PENDING_REVIEW")
                    .transactionDate(LocalDateTime.now())
                    .build();

            txList.add(tx);
        }

        workbook.close();
        return txList;
    }

    private List<Transaction> parseCSV(InputStream inputStream) throws Exception {
        List<Transaction> txList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        
        // Skip header
        String headerLine = reader.readLine();
        if (headerLine == null) {
            throw new IllegalArgumentException("CSV file is empty.");
        }

        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            
            // Regex split to handle commas inside quotes
            String[] tokens = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
            if (tokens.length < 4) continue;

            String accNum = cleanCSVToken(tokens[0]);
            String amtStr = cleanCSVToken(tokens[1]);
            String loc = cleanCSVToken(tokens[2]);
            String dev = cleanCSVToken(tokens[3]);

            if (accNum.isEmpty() || amtStr.isEmpty() || loc.isEmpty() || dev.isEmpty()) {
                continue; // Skip empty fields
            }

            BigDecimal amount;
            try {
                amount = new BigDecimal(amtStr.replace(",", "").replace("\"", ""));
                if (amount.compareTo(BigDecimal.ZERO) <= 0) continue;
            } catch (NumberFormatException e) {
                continue;
            }

            Transaction tx = Transaction.builder()
                    .accountNumber(accNum)
                    .amount(amount)
                    .location(loc)
                    .device(dev)
                    .status("PENDING_REVIEW")
                    .transactionDate(LocalDateTime.now())
                    .build();

            txList.add(tx);
        }
        
        reader.close();
        return txList;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        CellType type = cell.getCellType();
        if (type == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (type == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toString();
            }
            return String.valueOf(cell.getNumericCellValue());
        } else if (type == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        } else if (type == CellType.FORMULA) {
            return cell.getCellFormula();
        }
        return "";
    }

    private String cleanCSVToken(String token) {
        if (token == null) return "";
        token = token.trim();
        if (token.startsWith("\"") && token.endsWith("\"")) {
            token = token.substring(1, token.length() - 1);
        }
        return token.trim();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalTx = transactionRepository.countTotalTransactions();
        long fraudTx = fraudResultRepository.countByRiskLevel("CRITICAL") + fraudResultRepository.countByRiskLevel("HIGH");
        long mediumTx = fraudResultRepository.countByRiskLevel("MEDIUM");
        long lowTx = fraudResultRepository.countByRiskLevel("LOW");
        
        long activeUsers = userRepository.findAll().stream().filter(u -> u.isActive()).count();
        BigDecimal totalVolume = transactionRepository.sumTotalAmount();
        if (totalVolume == null) totalVolume = BigDecimal.ZERO;

        stats.put("totalTransactions", totalTx);
        stats.put("fraudulentTransactions", fraudTx);
        stats.put("mediumRiskTransactions", mediumTx);
        stats.put("safeTransactions", lowTx);
        stats.put("activeUsers", activeUsers);
        stats.put("totalVolume", totalVolume);
        
        // Compute Fraud Percentage
        double fraudPercentage = totalTx > 0 ? ((double) fraudTx / totalTx) * 100 : 0.0;
        stats.put("fraudPercentage", String.format("%.2f", fraudPercentage));

        // Distribution data for Chart.js
        List<Object[]> distribution = fraudResultRepository.getRiskDistribution();
        Map<String, Long> riskMap = new HashMap<>();
        riskMap.put("LOW", 0L);
        riskMap.put("MEDIUM", 0L);
        riskMap.put("HIGH", 0L);
        riskMap.put("CRITICAL", 0L);

        for (Object[] obj : distribution) {
            String level = (String) obj[0];
            Long count = (Long) obj[1];
            riskMap.put(level, count);
        }
        stats.put("riskDistribution", riskMap);

        return stats;
    }
}
