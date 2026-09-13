package com.example.fleetflowbe.controller;

import com.example.fleetflowbe.common.response.ApiResponse;
import com.example.fleetflowbe.common.response.PageResponse;
import com.example.fleetflowbe.dto.response.AuditLogResponse;
import com.example.fleetflowbe.entity.AuditLog;
import com.example.fleetflowbe.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Audit Log", description = "Truy vết nhật ký thao tác đối tượng và an ninh hệ thống")
@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @Operation(summary = "Xem lịch sử thay đổi (Audit trail) của một đối tượng nghiệp vụ")
    @GetMapping("/entity/{entityName}/{entityId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getEntityLogs(
            @PathVariable String entityName,
            @PathVariable String entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var pageResult = auditLogService.getLogsForEntity(entityName, entityId, PageRequest.of(page, size))
                .map(this::mapToResponse);

        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(pageResult)));
    }

    private AuditLogResponse mapToResponse(AuditLog a) {
        return AuditLogResponse.builder()
                .id(a.getId())
                .actorId(a.getActorId())
                .actorUsername(a.getActorUsername())
                .actorRole(a.getActorRole())
                .action(a.getAction())
                .entityName(a.getEntityName())
                .entityId(a.getEntityId())
                .oldDataJson(a.getOldDataJson())
                .newDataJson(a.getNewDataJson())
                .ipAddress(a.getIpAddress())
                .performedAt(a.getPerformedAt())
                .build();
    }
}

