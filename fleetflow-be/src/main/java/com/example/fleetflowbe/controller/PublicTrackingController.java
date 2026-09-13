package com.example.fleetflowbe.controller;

import com.example.fleetflowbe.common.response.ApiResponse;
import com.example.fleetflowbe.dto.response.ShipmentTrackingResponse;
import com.example.fleetflowbe.service.TrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Public Tracking", description = "Tra cứu hành trình vận đơn công khai cho khách hàng (Read-heavy API)")
@RestController
@RequestMapping("/api/v1/public/tracking")
@RequiredArgsConstructor
public class PublicTrackingController {

    private final TrackingService trackingService;

    @Operation(summary = "Tra cứu chi tiết lộ trình đơn hàng theo mã vận đơn (Không cần đăng nhập)")
    @GetMapping("/{trackingCode}")
    public ResponseEntity<ApiResponse<ShipmentTrackingResponse>> trackShipment(@PathVariable String trackingCode) {
        ShipmentTrackingResponse response = trackingService.getPublicTracking(trackingCode);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}

