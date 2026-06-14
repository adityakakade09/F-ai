package com.fraudguard.service.impl;

import com.fraudguard.model.FraudResult;
import com.fraudguard.model.Transaction;
import com.fraudguard.repository.FraudResultRepository;
import com.fraudguard.repository.TransactionRepository;
import com.fraudguard.service.ReportService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.alignment.HorizontalAlignment;
import com.lowagie.text.pdf.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private FraudResultRepository fraudResultRepository;

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePDFReport() throws Exception {
        List<Transaction> transactions = transactionRepository.findAll();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        Document document = new Document(PageSize.A4, 36, 36, 54, 54);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        
        // Add footer handler
        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter writer, Document document) {
                PdfContentByte cb = writer.getDirectContent();
                cb.saveState();
                cb.beginText();
                try {
                    cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED), 8);
                } catch (Exception e) {
                    // ignore
                }
                cb.setColorFill(Color.GRAY);
                cb.showTextAligned(PdfContentByte.ALIGN_CENTER, 
                        "Page " + writer.getPageNumber() + " | FraudGuard AI Enterprise Report", 
                        (document.right() - document.left()) / 2 + document.leftMargin(), 
                        document.bottom() - 10, 0);
                cb.endText();
                cb.restoreState();
            }
        });

        document.open();

        // 1. Header Banner
        Font titleFont = new Font(Font.HELVETICA, 22, Font.BOLD, new Color(30, 41, 59));
        Font subtitleFont = new Font(Font.HELVETICA, 10, Font.ITALIC, Color.GRAY);
        
        Paragraph title = new Paragraph("FRAUDGUARD AI - COMPREHENSIVE FRAUD AUDIT REPORT", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph subtitle = new Paragraph("Generated on: " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + 
                " | Confidential", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);

        // 2. Metrics summary
        long totalTx = transactionRepository.countTotalTransactions();
        long criticalTx = fraudResultRepository.countByRiskLevel("CRITICAL");
        long highTx = fraudResultRepository.countByRiskLevel("HIGH");
        long mediumTx = fraudResultRepository.countByRiskLevel("MEDIUM");
        long lowTx = fraudResultRepository.countByRiskLevel("LOW");
        BigDecimal totalVolume = transactionRepository.sumTotalAmount();
        if (totalVolume == null) totalVolume = BigDecimal.ZERO;

        Font statsHeaderFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
        Font statsContentFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);

        PdfPTable statsTable = new PdfPTable(3);
        statsTable.setWidthPercentage(100);
        statsTable.setSpacingAfter(20);

        // Header Background Color
        Color primaryColor = new Color(79, 70, 229); // indigo-600

        addTableHeaderCell(statsTable, "Total Transactions Evaluated", primaryColor, statsHeaderFont);
        addTableHeaderCell(statsTable, "Total Fraud Risk (High/Critical)", primaryColor, statsHeaderFont);
        addTableHeaderCell(statsTable, "Total Evaluated Volume", primaryColor, statsHeaderFont);

        statsTable.addCell(new PdfPCell(new Phrase(String.valueOf(totalTx), statsContentFont)));
        statsTable.addCell(new PdfPCell(new Phrase(String.valueOf(criticalTx + highTx), statsContentFont)));
        statsTable.addCell(new PdfPCell(new Phrase("$" + totalVolume, statsContentFont)));
        
        document.add(statsTable);

        // 3. Transactions details table
        Paragraph tableHeader = new Paragraph("Detailed Transaction Log & Risk Breakdown", 
                new Font(Font.HELVETICA, 14, Font.BOLD, new Color(30, 41, 59)));
        tableHeader.setSpacingAfter(10);
        document.add(tableHeader);

        PdfPTable detailsTable = new PdfPTable(7);
        detailsTable.setWidthPercentage(100);
        detailsTable.setWidths(new float[]{1.0f, 2.5f, 1.8f, 2.0f, 2.0f, 1.5f, 1.8f});

        Color navyHeader = new Color(15, 23, 42); // slate-900
        Font detailsHeaderFont = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
        Font detailsContentFont = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.BLACK);

        addTableHeaderCell(detailsTable, "ID", navyHeader, detailsHeaderFont);
        addTableHeaderCell(detailsTable, "Account Number", navyHeader, detailsHeaderFont);
        addTableHeaderCell(detailsTable, "Amount", navyHeader, detailsHeaderFont);
        addTableHeaderCell(detailsTable, "Location", navyHeader, detailsHeaderFont);
        addTableHeaderCell(detailsTable, "Device", navyHeader, detailsHeaderFont);
        addTableHeaderCell(detailsTable, "Score", navyHeader, detailsHeaderFont);
        addTableHeaderCell(detailsTable, "Risk Level", navyHeader, detailsHeaderFont);

        for (Transaction tx : transactions) {
            FraudResult res = fraudResultRepository.findByTransactionId(tx.getId()).orElse(null);
            double score = res != null ? res.getFraudScore() : 0.0;
            String risk = res != null ? res.getRiskLevel() : "UNKNOWN";

            detailsTable.addCell(new PdfPCell(new Phrase(String.valueOf(tx.getId()), detailsContentFont)));
            detailsTable.addCell(new PdfPCell(new Phrase(tx.getAccountNumber(), detailsContentFont)));
            detailsTable.addCell(new PdfPCell(new Phrase("$" + tx.getAmount(), detailsContentFont)));
            detailsTable.addCell(new PdfPCell(new Phrase(tx.getLocation(), detailsContentFont)));
            detailsTable.addCell(new PdfPCell(new Phrase(tx.getDevice(), detailsContentFont)));
            detailsTable.addCell(new PdfPCell(new Phrase(String.format("%.1f", score), detailsContentFont)));

            // Color code the Risk Level cell
            PdfPCell riskCell = new PdfPCell(new Phrase(risk, detailsContentFont));
            if ("CRITICAL".equalsIgnoreCase(risk)) {
                riskCell.setBackgroundColor(new Color(254, 226, 226)); // light red
            } else if ("HIGH".equalsIgnoreCase(risk)) {
                riskCell.setBackgroundColor(new Color(255, 237, 213)); // light orange
            } else if ("MEDIUM".equalsIgnoreCase(risk)) {
                riskCell.setBackgroundColor(new Color(254, 249, 195)); // light yellow
            } else {
                riskCell.setBackgroundColor(new Color(240, 253, 244)); // light green
            }
            detailsTable.addCell(riskCell);
        }

        document.add(detailsTable);
        document.close();

        return baos.toByteArray();
    }

    private void addTableHeaderCell(PdfPTable table, String text, Color bg, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateExcelReport() throws Exception {
        List<Transaction> transactions = transactionRepository.findAll();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Transactions Summary");

        // Create Fonts & Styles
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        // Header Row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Transaction ID", "Account Number", "Amount", "Date", "Location", "Device", "Status", "Fraud Score", "Risk Level", "Fraud Reason"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data Rows
        int rowIdx = 1;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Transaction tx : transactions) {
            Row row = sheet.createRow(rowIdx++);
            
            FraudResult res = fraudResultRepository.findByTransactionId(tx.getId()).orElse(null);
            double score = res != null ? res.getFraudScore() : 0.0;
            String risk = res != null ? res.getRiskLevel() : "UNKNOWN";
            String reason = res != null ? res.getFraudReason() : "N/A";

            row.createCell(0).setCellValue(tx.getId());
            row.createCell(1).setCellValue(tx.getAccountNumber());
            row.createCell(2).setCellValue(tx.getAmount().doubleValue());
            row.createCell(3).setCellValue(tx.getTransactionDate().format(dtf));
            row.createCell(4).setCellValue(tx.getLocation());
            row.createCell(5).setCellValue(tx.getDevice());
            row.createCell(6).setCellValue(tx.getStatus());
            row.createCell(7).setCellValue(score);
            row.createCell(8).setCellValue(risk);
            row.createCell(9).setCellValue(reason);
        }

        // Auto-size Columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(baos);
        workbook.close();

        return baos.toByteArray();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateCSVReport() throws Exception {
        List<Transaction> transactions = transactionRepository.findAll();
        StringBuilder csv = new StringBuilder();

        // Header
        csv.append("Transaction ID,Account Number,Amount,Date,Location,Device,Status,Fraud Score,Risk Level,Fraud Reason\n");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Transaction tx : transactions) {
            FraudResult res = fraudResultRepository.findByTransactionId(tx.getId()).orElse(null);
            double score = res != null ? res.getFraudScore() : 0.0;
            String risk = res != null ? res.getRiskLevel() : "UNKNOWN";
            String reason = res != null ? res.getFraudReason() : "N/A";

            csv.append(tx.getId()).append(",")
               .append(escapeCSV(tx.getAccountNumber())).append(",")
               .append(tx.getAmount().doubleValue()).append(",")
               .append(tx.getTransactionDate().format(dtf)).append(",")
               .append(escapeCSV(tx.getLocation())).append(",")
               .append(escapeCSV(tx.getDevice())).append(",")
               .append(escapeCSV(tx.getStatus())).append(",")
               .append(score).append(",")
               .append(escapeCSV(risk)).append(",")
               .append(escapeCSV(reason)).append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }
}
import jakarta.servlet.http.HttpServletResponse;
