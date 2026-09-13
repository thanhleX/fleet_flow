package com.example.fleetflowbe.service;

import com.example.fleetflowbe.common.constants.RegionZone;
import com.example.fleetflowbe.common.constants.ServiceTier;
import com.example.fleetflowbe.dto.request.PricingCalculateRequest;
import com.example.fleetflowbe.dto.response.PricingEstimateResponse;
import com.example.fleetflowbe.entity.PricingRule;
import com.example.fleetflowbe.repository.PricingRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingEngineService {

    private final PricingRuleRepository pricingRuleRepository;

    @Value("${fleetflow.pricing.volumetric-divisor:5000.0}")
    private double volumetricDivisor;

    @Value("${fleetflow.pricing.cod-fee-rate:0.008}")
    private double defaultCodFeeRate;

    @Value("${fleetflow.pricing.cod-fee-min:15000.0}")
    private double defaultCodFeeMin;

    @Value("${fleetflow.pricing.insurance-rate:0.005}")
    private double defaultInsuranceRate;

    /**
     * Tính toán trọng lượng quy đổi thể tích (Volumetric Weight):
     * Formula: (Length * Width * Height) / 5000
     */
    public double calculateVolumetricWeight(Double lengthCm, Double widthCm, Double heightCm) {
        if (lengthCm == null || widthCm == null || heightCm == null ||
                lengthCm <= 0 || widthCm <= 0 || heightCm <= 0) {
            return 0.0;
        }
        double volWeight = (lengthCm * widthCm * heightCm) / volumetricDivisor;
        return Math.round(volWeight * 100.0) / 100.0;
    }

    /**
     * Trọng lượng tính cước là giá trị lớn hơn giữa Trọng lượng thực tế và Trọng lượng thể tích.
     */
    public double calculateChargeableWeight(Double actualWeightKg, Double lengthCm, Double widthCm, Double heightCm) {
        double volWeight = calculateVolumetricWeight(lengthCm, widthCm, heightCm);
        return Math.max(actualWeightKg != null ? actualWeightKg : 0.0, volWeight);
    }

    @Transactional(readOnly = true)
    public PricingEstimateResponse estimateShippingFee(PricingCalculateRequest request) {
        RegionZone zone = request.getRegionZone();
        ServiceTier tier = request.getServiceTier();

        PricingRule rule = pricingRuleRepository.findByRegionZoneAndServiceTier(zone, tier)
                .orElseGet(() -> PricingRule.builder()
                        .regionZone(zone)
                        .serviceTier(tier)
                        .basePrice(BigDecimal.valueOf(25000.0))
                        .baseWeightKg(2.0)
                        .stepPricePerKg(BigDecimal.valueOf(6000.0))
                        .codFeePercentage(BigDecimal.valueOf(defaultCodFeeRate))
                        .codFeeMin(BigDecimal.valueOf(defaultCodFeeMin))
                        .insuranceRatePercentage(BigDecimal.valueOf(defaultInsuranceRate))
                        .build());

        double volWeight = calculateVolumetricWeight(request.getLengthCm(), request.getWidthCm(), request.getHeightCm());
        double chargeableWeight = Math.max(request.getWeightKg(), volWeight);

        // 1. Phí vận chuyển cơ bản
        BigDecimal baseShippingFee = rule.getBasePrice();
        if (chargeableWeight > rule.getBaseWeightKg()) {
            double extraKg = Math.ceil(chargeableWeight - rule.getBaseWeightKg());
            BigDecimal extraFee = rule.getStepPricePerKg().multiply(BigDecimal.valueOf(extraKg));
            baseShippingFee = baseShippingFee.add(extraFee);
        }

        // 2. Phí thu hộ COD
        BigDecimal codFee = BigDecimal.ZERO;
        if (request.getCodAmount() != null && request.getCodAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal feePercentage = rule.getCodFeePercentage() != null ? rule.getCodFeePercentage() : BigDecimal.valueOf(defaultCodFeeRate);
            BigDecimal calculatedCodFee = request.getCodAmount().multiply(feePercentage).setScale(0, RoundingMode.HALF_UP);
            BigDecimal minCodFee = rule.getCodFeeMin() != null ? rule.getCodFeeMin() : BigDecimal.valueOf(defaultCodFeeMin);
            codFee = calculatedCodFee.max(minCodFee);
        }

        // 3. Phí bảo hiểm giá trị hàng hoá (áp dụng khi khai giá > 1,000,000 VND)
        BigDecimal insuranceFee = BigDecimal.ZERO;
        if (request.getDeclaredValue() != null && request.getDeclaredValue().compareTo(BigDecimal.valueOf(1000000)) > 0) {
            BigDecimal insRate = rule.getInsuranceRatePercentage() != null ? rule.getInsuranceRatePercentage() : BigDecimal.valueOf(defaultInsuranceRate);
            insuranceFee = request.getDeclaredValue().multiply(insRate).setScale(0, RoundingMode.HALF_UP);
        }

        BigDecimal totalEstimatedFee = baseShippingFee.add(codFee).add(insuranceFee);

        return PricingEstimateResponse.builder()
                .regionZone(zone)
                .serviceTier(tier)
                .actualWeightKg(request.getWeightKg())
                .volumetricWeightKg(volWeight)
                .chargeableWeightKg(chargeableWeight)
                .baseShippingFee(baseShippingFee)
                .codFee(codFee)
                .insuranceFee(insuranceFee)
                .totalEstimatedFee(totalEstimatedFee)
                .build();
    }
}

