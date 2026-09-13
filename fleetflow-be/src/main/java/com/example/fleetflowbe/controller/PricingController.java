package com.example.fleetflowbe.controller;

import com.example.fleetflowbe.common.response.ApiResponse;
import com.example.fleetflowbe.dto.request.PricingCalculateRequest;
import com.example.fleetflowbe.dto.response.PricingEstimateResponse;
import com.example.fleetflowbe.service.PricingEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Pricing Engine", description = "Ước tính cước vận chuyển, tính trọng lượng quy đổi và phụ phí")
@RestController
@RequestMapping("/api/v1/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final PricingEngineService pricingEngineService;

    @Operation(summary = "Ước tính cước phí vận chuyển dựa trên trọng lượng, khu vực và gói dịch vụ")
    @PostMapping("/estimate")
    public ResponseEntity<ApiResponse<PricingEstimateResponse>> estimateFee(@Valid @RequestBody PricingCalculateRequest request) {
        PricingEstimateResponse response = pricingEngineService.estimateShippingFee(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}

