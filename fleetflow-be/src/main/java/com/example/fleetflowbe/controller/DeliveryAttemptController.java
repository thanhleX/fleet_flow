package com.example.fleetflowbe.controller;

import com.example.fleetflowbe.common.response.ApiResponse;
import com.example.fleetflowbe.dto.request.DeliveryAttemptRequest;
import com.example.fleetflowbe.dto.response.DeliveryAttemptResponse;
import com.example.fleetflowbe.service.DeliveryAttemptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Delivery Attempts", description = "Ghi nhận kết quả giao hàng (Thành công / Thất bại) và lịch sử thử giao")
@RestController
@RequestMapping("/api/v1/shipments/{shipmentId}/attempts")
@RequiredArgsConstructor
public class DeliveryAttemptController {

    private final DeliveryAttemptService deliveryAttemptService;

    @Operation(summary = "Ghi nhận kết quả một lần giao hàng (Tài xế thực hiện)")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or (hasRole('DRIVER') and @securityEvaluator.isDriverAssigned(#shipmentId, authentication))")
    public ResponseEntity<ApiResponse<DeliveryAttemptResponse>> recordAttempt(
            @PathVariable Long shipmentId,
            @Valid @RequestBody DeliveryAttemptRequest request) {
        DeliveryAttemptResponse response = deliveryAttemptService.recordAttempt(shipmentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "Ghi nhận kết quả giao hàng thành công"));
    }

    @Operation(summary = "Lấy toàn bộ lịch sử các lần giao hàng của đơn")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF') or (hasRole('DRIVER') and @securityEvaluator.isDriverAssigned(#shipmentId, authentication))")
    public ResponseEntity<ApiResponse<List<DeliveryAttemptResponse>>> getAttempts(@PathVariable Long shipmentId) {
        List<DeliveryAttemptResponse> attempts = deliveryAttemptService.getAttemptsForShipment(shipmentId);
        return ResponseEntity.ok(ApiResponse.ok(attempts));
    }
}

