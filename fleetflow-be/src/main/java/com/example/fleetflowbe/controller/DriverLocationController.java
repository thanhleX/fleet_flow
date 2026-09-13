package com.example.fleetflowbe.controller;

import com.example.fleetflowbe.common.response.ApiResponse;
import com.example.fleetflowbe.dto.request.DriverLocationPingRequest;
import com.example.fleetflowbe.entity.DriverLocation;
import com.example.fleetflowbe.service.DriverLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Driver Telemetry", description = "Cập nhật vị trí GPS thời gian thực và lịch sử di chuyển tài xế")
@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
public class DriverLocationController {

    private final DriverLocationService driverLocationService;

    @Operation(summary = "Tài xế gửi toạ độ GPS định kỳ (Ping vị trí)")
    @PostMapping("/{driverId}/location")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or (hasRole('DRIVER') and @securityEvaluator.isOwnDriverProfile(#driverId, authentication))")
    public ResponseEntity<ApiResponse<Void>> pingLocation(
            @PathVariable Long driverId,
            @Valid @RequestBody DriverLocationPingRequest request) {
        driverLocationService.recordLocationPing(driverId, request);
        return ResponseEntity.ok(ApiResponse.ok(null, "Cập nhật toạ độ GPS thành công"));
    }

    @Operation(summary = "Xem vết di chuyển (trail) gần đây của tài xế")
    @GetMapping("/{driverId}/trail")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<List<DriverLocation>>> getDriverTrail(
            @PathVariable Long driverId,
            @RequestParam(defaultValue = "12") int hours) {
        List<DriverLocation> trail = driverLocationService.getDriverRecentTrail(driverId, hours);
        return ResponseEntity.ok(ApiResponse.ok(trail));
    }
}

