package com.example.fleetflowbe.service;

import com.example.fleetflowbe.entity.AuditLog;
import com.example.fleetflowbe.repository.AuditLogRepository;
import com.example.fleetflowbe.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void record(String action, String entityName, String entityId, String oldDataJson, String newDataJson) {
        Long actorId = null;
        String actorUsername = "SYSTEM";
        String actorRole = "SYSTEM";

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            actorId = principal.getId();
            actorUsername = principal.getUsername();
            actorRole = principal.getRole().name();
        }

        AuditLog log = AuditLog.builder()
                .actorId(actorId)
                .actorUsername(actorUsername)
                .actorRole(actorRole)
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .oldDataJson(oldDataJson)
                .newDataJson(newDataJson)
                .performedAt(LocalDateTime.now())
                .build();

        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getLogsForEntity(String entityName, String entityId, Pageable pageable) {
        return auditLogRepository.findByEntityNameAndEntityIdOrderByPerformedAtDesc(entityName, entityId, pageable);
    }
}

