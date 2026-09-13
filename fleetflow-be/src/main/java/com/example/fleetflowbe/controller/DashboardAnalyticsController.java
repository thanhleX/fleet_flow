package com.example.fleetflowbe.controller;

import com.example.fleetflowbe.common.constants.Role;
import com.example.fleetflowbe.common.response.ApiResponse;
import com.example.fleetflowbe.dto.response.DashboardStatsResponse;
import com.example.fleetflowbe.security.UserPrincipal;
import com.example.fleetflowbe.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@Tag(name = "Dashboard & Analytics", description = "Thống kê hiệu suất vận hành, doanh thu cước, dòng tiền COD và tỷ lệ giao thành công")
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class DashboardAnalyticsController {

    private final DashboardService dashboardService;

    @Operation(summary = "Lấy báo cáo tổng hợp chỉ số vận hành và tài chính (Dashboard)")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats(
            @AuthenticationPrincipal UserPrincipal principal) {
        // Chức năng dành riêng cho Quản trị viên và Điều phối viên (ADMIN, MANAGER, STAFF)
        if (principal != null && principal.getRole() == Role.ROLE_DRIVER) {
            return ResponseEntity.ok(ApiResponse.ok(
                    DashboardStatsResponse.builder()
                            .totalShipments(0)
                            .countByStatus(Map.of())
                            .deliverySuccessRatePercent(0.0)
                            .totalShippingRevenue(BigDecimal.ZERO)
                            .totalDeliveredCodVolume(BigDecimal.ZERO)
                            .activeDriversCount(0)
                            .pendingUnassignedShipments(0)
                            .build(),
                    "Chức năng dành riêng cho Quản trị viên và Nhân viên điều phối."
            ));
        }
        DashboardStatsResponse response = dashboardService.getManagementDashboard();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}

