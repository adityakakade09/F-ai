package com.fraudguard.service.impl;

import com.fraudguard.model.AuditLog;
import com.fraudguard.repository.AuditLogRepository;
import com.fraudguard.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Async // Asynchronous logging for better performance
    @Override
    @Transactional
    public void logAction(String username, String action, String ipAddress, String browser, String module) {
        String cleanBrowser = browser != null ? browser : "Unknown Browser";
        if (cleanBrowser.length() > 150) {
            cleanBrowser = cleanBrowser.substring(0, 150);
        }
        
        AuditLog log = AuditLog.builder()
                .username(username)
                .action(action)
                .ipAddress(ipAddress != null ? ipAddress : "127.0.0.1")
                .browser(cleanBrowser)
                .module(module)
                .build();
        auditLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(String query, Pageable pageable) {
        if (query != null && !query.trim().isEmpty()) {
            return auditLogRepository.findByUsernameContainingIgnoreCaseOrActionContainingIgnoreCaseOrModuleContainingIgnoreCase(
                    query, query, query, pageable);
        }
        return auditLogRepository.findAll(pageable);
    }
}
