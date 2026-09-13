package com.example.fleetflowbe.dto.response;

import com.example.fleetflowbe.common.constants.SettlementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodSettlementResponse {
    private Long id;
    private String settlementCode;
    private String idempotencyKey;
    private Long driverId;
    private String driverName;
    private Long verifiedByStaffId;
    private String verifiedByStaffName;
    private BigDecimal totalAmount;
    private Integer totalOrders;
    private SettlementStatus status;
    private LocalDateTime settledAt;
    private String note;
    private List<CodTransactionResponse> transactions;
}

