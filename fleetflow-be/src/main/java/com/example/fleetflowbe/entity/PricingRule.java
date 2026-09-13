package com.example.fleetflowbe.entity;

import com.example.fleetflowbe.common.constants.RegionZone;
import com.example.fleetflowbe.common.constants.ServiceTier;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pricing_rules", indexes = {
        @Index(name = "idx_pricing_zone_tier", columnList = "region_zone, service_tier", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingRule extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "region_zone", nullable = false, length = 30)
    private RegionZone regionZone;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_tier", nullable = false, length = 30)
    private ServiceTier serviceTier;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "base_weight_kg", nullable = false)
    private Double baseWeightKg;

    @Column(name = "step_price_per_kg", nullable = false, precision = 12, scale = 2)
    private BigDecimal stepPricePerKg;

    @Column(name = "cod_fee_percentage", precision = 6, scale = 4)
    private BigDecimal codFeePercentage;

    @Column(name = "cod_fee_min", precision = 12, scale = 2)
    private BigDecimal codFeeMin;

    @Column(name = "insurance_rate_percentage", precision = 6, scale = 4)
    private BigDecimal insuranceRatePercentage;
}

