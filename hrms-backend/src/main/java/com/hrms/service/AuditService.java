package com.hrms.service;

import com.hrms.common.TenantContextHolder;
import com.hrms.entity.AuditLog;
import com.hrms.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void logAction(UUID actorId, String actorEmail, String action, String entityName, UUID entityId, String details, String ipAddress) {
        AuditLog log = AuditLog.builder()
                .companyId(TenantContextHolder.getTenantId())
                .actorId(actorId)
                .actorEmail(actorEmail)
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .details(details)
                .ipAddress(ipAddress)
                .createdAt(Instant.now())
                .build();
        auditLogRepository.save(log);
    }
}
