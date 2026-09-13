package com.example.fleetflowbe.service;

import com.example.fleetflowbe.common.constants.DriverStatus;
import com.example.fleetflowbe.common.constants.ShipmentStatus;
import com.example.fleetflowbe.dto.response.DashboardStatsResponse;
import com.example.fleetflowbe.repository.DriverRepository;
import com.example.fleetflowbe.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ShipmentRepository shipmentRepository;
    private final DriverRepository driverRepository;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getManagementDashboard() {
        // 1. Thống kê số lượng đơn theo từng trạng thái
        List<Object[]> statusCounts = shipmentRepository.countShipmentsGroupedByStatus();
        Map<ShipmentStatus, Long> countByStatus = new EnumMap<>(ShipmentStatus.class);
        long totalShipments = 0;

        for (Object[] row : statusCounts) {
            if (row == null || row[0] == null) continue;
            ShipmentStatus st;
            if (row[0] instanceof ShipmentStatus shipmentStatus) {
                st = shipmentStatus;
            } else {
                st = ShipmentStatus.fromString(row[0].toString());
            }
            Long count = row[1] instanceof Number num ? num.longValue() : 0L;
            countByStatus.put(st, count);
            totalShipments += count;
        }

        // 2. Doanh thu phí vận chuyển và tổng COD đã thu
        BigDecimal totalRevenue = shipmentRepository.calculateTotalDeliveredShippingRevenue();
        BigDecimal totalCod = shipmentRepository.calculateTotalDeliveredCodVolume();

        // 3. Tỷ lệ giao thành công
        long deliveredCount = shipmentRepository.countByStatus(ShipmentStatus.DELIVERED);
        long returnedCount = shipmentRepository.countByStatus(ShipmentStatus.RETURNED);
        long finishedOrders = deliveredCount + returnedCount;

        double successRate = 0.0;
        if (finishedOrders > 0) {
            successRate = Math.round(((double) deliveredCount / finishedOrders) * 10000.0) / 100.0;
        }

        // 4. Tài xế đang hoạt động và số đơn chờ gán
        long activeDrivers = driverRepository.findByStatus(DriverStatus.AVAILABLE).size()
                + driverRepository.findByStatus(DriverStatus.BUSY).size();
        long unassignedOrders = shipmentRepository.findUnassignedShipments().size();

        return DashboardStatsResponse.builder()
                .totalShipments(totalShipments)
                .countByStatus(countByStatus)
                .deliverySuccessRatePercent(successRate)
                .totalShippingRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .totalDeliveredCodVolume(totalCod != null ? totalCod : BigDecimal.ZERO)
                .activeDriversCount(activeDrivers)
                .pendingUnassignedShipments(unassignedOrders)
                .build();
    }
}

