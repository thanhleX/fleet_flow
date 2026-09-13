package com.example.fleetflowbe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long id;
    private Long actorId;
    private String actorUsername;
    private String actorRole;
    private String action;
    private String entityName;
    private String entityId;
    private String oldDataJson;
    private String newDataJson;
    private String ipAddress;
    private LocalDateTime performedAt;
}

