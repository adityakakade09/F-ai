package com.fraudguard.service;

import com.fraudguard.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {
    void logAction(String username, String action, String ipAddress, String browser, String module);
    Page<AuditLog> getAuditLogs(String query, Pageable pageable);
}
