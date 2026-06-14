package com.fraudguard.repository;

import com.fraudguard.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByUsernameContainingIgnoreCaseOrActionContainingIgnoreCaseOrModuleContainingIgnoreCase(
            String username, String action, String module, Pageable pageable);
}
