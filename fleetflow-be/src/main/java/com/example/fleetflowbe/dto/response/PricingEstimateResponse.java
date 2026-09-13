package com.example.fleetflowbe.dto.response;

import com.example.fleetflowbe.common.constants.RegionZone;
import com.example.fleetflowbe.common.constants.ServiceTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingEstimateResponse {
    private RegionZone regionZone;
    private ServiceTier serviceTier;
    private Double actualWeightKg;
    private Double volumetricWeightKg;
    private Double chargeableWeightKg;
    private BigDecimal baseShippingFee;
    private BigDecimal codFee;
    private BigDecimal insuranceFee;
    private BigDecimal totalEstimatedFee;
}

