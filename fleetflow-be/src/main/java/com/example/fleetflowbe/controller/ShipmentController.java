package com.example.fleetflowbe.controller;

import com.example.fleetflowbe.common.constants.ShipmentStatus;
import com.example.fleetflowbe.common.response.ApiResponse;
import com.example.fleetflowbe.common.response.PageResponse;
import com.example.fleetflowbe.dto.request.CreateShipmentRequest;
import com.example.fleetflowbe.dto.request.UpdateShipmentStatusRequest;
import com.example.fleetflowbe.dto.response.ShipmentResponse;
import com.example.fleetflowbe.security.UserPrincipal;
import com.example.fleetflowbe.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Shipment Management", description = "Quản lý tạo đơn, cập nhật trạng thái và danh sách vận đơn")
@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @Operation(summary = "Tạo mới đơn vận chuyển (Shipment)")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<ShipmentResponse>> createShipment(@Valid @RequestBody CreateShipmentRequest request) {
        ShipmentResponse response = shipmentService.createShipment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "Tạo đơn vận chuyển thành công"));
    }

    @Operation(summary = "Xem chi tiết đơn vận chuyển theo ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF') or (hasRole('DRIVER') and @securityEvaluator.isDriverAssigned(#id, authentication))")
    public ResponseEntity<ApiResponse<ShipmentResponse>> getShipmentById(@PathVariable Long id) {
        ShipmentResponse response = shipmentService.getShipmentById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Cập nhật trạng thái đơn vận chuyển")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF') or (hasRole('DRIVER') and @securityEvaluator.isDriverAssigned(#id, authentication))")
    public ResponseEntity<ApiResponse<ShipmentResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateShipmentStatusRequest request) {
        ShipmentResponse response = shipmentService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Cập nhật trạng thái thành công"));
    }

    @Operation(summary = "Danh sách đơn hàng theo trạng thái (Phân trang)")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<PageResponse<ShipmentResponse>>> getShipmentsByStatus(
            @PathVariable ShipmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageResult = shipmentService.getShipmentsByStatus(status,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(pageResult)));
    }

    @Operation(summary = "Tài xế xem danh sách nhiệm vụ được gán cho chính mình, hoặc Quản trị/Điều phối viên xem theo driverId")
    @GetMapping("/driver/my-tasks")
    public ResponseEntity<ApiResponse<PageResponse<ShipmentResponse>>> getMyAssignedTasks(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long driverId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long targetDriverId = (principal != null) ? principal.getDriverId() : null;

        // Chức năng dành riêng cho tài xế (ROLE_DRIVER). Nếu các role khác (ADMIN, MANAGER, STAFF) truy cập:
        if (targetDriverId == null) {
            // Cho phép Admin/Staff xem theo driverId nếu có truyền vào (ví dụ kiểm thử hoặc phân tích)
            if (driverId != null) {
                targetDriverId = driverId;
            } else {
                // Trả về danh sách rỗng an toàn, tránh ném HTTP 403 gây báo lỗi đỏ trên giao diện
                return ResponseEntity.ok(ApiResponse.ok(
                        PageResponse.empty(),
                        "Chức năng này dành riêng cho Tài xế. Tài khoản hiện tại không có nhiệm vụ được phân công."
                ));
            }
        }

        var pageResult = shipmentService.getShipmentsByDriver(targetDriverId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(pageResult)));
    }
}

