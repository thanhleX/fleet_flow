package com.example.fleetflowbe.controller;

import com.example.fleetflowbe.common.response.ApiResponse;
import com.example.fleetflowbe.dto.request.AssignDriverRequest;
import com.example.fleetflowbe.dto.response.ShipmentResponse;
import com.example.fleetflowbe.service.DispatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Dispatch Management", description = "Điều phối và phân công đơn hàng cho tài xế với xử lý Concurrency")
@RestController
@RequestMapping("/api/v1/dispatch")
@RequiredArgsConstructor
public class DispatchController {

    private final DispatchService dispatchService;

    @Operation(summary = "Phân công tài xế cho đơn hàng (Optimistic & Pessimistic Locking)")
    @PostMapping("/{shipmentId}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<ShipmentResponse>> assignDriver(
            @PathVariable Long shipmentId,
            @Valid @RequestBody AssignDriverRequest request) {
        ShipmentResponse response = dispatchService.assignDriver(shipmentId, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Phân công tài xế thành công"));
    }

    @Operation(summary = "Hủy phân công tài xế (Đưa đơn về lại PENDING)")
    @DeleteMapping("/{shipmentId}/unassign")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<ShipmentResponse>> unassignDriver(@PathVariable Long shipmentId) {
        ShipmentResponse response = dispatchService.unassignDriver(shipmentId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Đã hủy phân công đơn hàng"));
    }
}

