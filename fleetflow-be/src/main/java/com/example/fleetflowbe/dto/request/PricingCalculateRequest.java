package com.example.fleetflowbe.dto.request;

import com.example.fleetflowbe.common.constants.RegionZone;
import com.example.fleetflowbe.common.constants.ServiceTier;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingCalculateRequest {

    @NotNull(message = "Khu vực tính cước không được để trống")
    private RegionZone regionZone;

    @NotNull(message = "Gói dịch vụ không được để trống")
    private ServiceTier serviceTier;

    @NotNull(message = "Khối lượng thực tế không được để trống")
    @DecimalMin(value = "0.01", message = "Khối lượng phải lớn hơn 0")
    private Double weightKg;

    private Double lengthCm;
    private Double widthCm;
    private Double heightCm;

    private BigDecimal declaredValue;
    private BigDecimal codAmount;
}

