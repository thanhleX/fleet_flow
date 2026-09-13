package com.example.fleetflowbe.dto.response;

import com.example.fleetflowbe.common.constants.CodStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodTransactionResponse {
    private Long id;
    private Long shipmentId;
    private String trackingCode;
    private BigDecimal amount;
    private CodStatus status;
    private Long driverId;
    private String driverName;
    private Long settlementId;
    private LocalDateTime collectedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime verifiedAt;
    private String disputeReason;
}

