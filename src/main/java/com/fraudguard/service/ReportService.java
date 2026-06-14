package com.fraudguard.service;

public interface ReportService {
    byte[] generatePDFReport() throws Exception;
    byte[] generateExcelReport() throws Exception;
    byte[] generateCSVReport() throws Exception;
}
