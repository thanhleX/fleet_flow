package com.example.fleetflowbe.dto.response;

import com.example.fleetflowbe.common.constants.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    private long totalShipments;
    private Map<ShipmentStatus, Long> countByStatus;
    private double deliverySuccessRatePercent;
    private BigDecimal totalShippingRevenue;
    private BigDecimal totalDeliveredCodVolume;
    private long activeDriversCount;
    private long pendingUnassignedShipments;
}

