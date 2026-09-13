package com.example.fleetflowbe.service;

import com.example.fleetflowbe.common.constants.DriverStatus;
import com.example.fleetflowbe.common.constants.ShipmentStatus;
import com.example.fleetflowbe.common.exception.BusinessException;
import com.example.fleetflowbe.common.exception.OptimisticLockingException;
import com.example.fleetflowbe.common.exception.ResourceNotFoundException;
import com.example.fleetflowbe.dto.request.AssignDriverRequest;
import com.example.fleetflowbe.dto.response.ShipmentResponse;
import com.example.fleetflowbe.entity.Driver;
import com.example.fleetflowbe.entity.Shipment;
import com.example.fleetflowbe.event.ShipmentAssignedEvent;
import com.example.fleetflowbe.repository.DriverRepository;
import com.example.fleetflowbe.repository.ShipmentRepository;
import com.example.fleetflowbe.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchService {

    private final ShipmentRepository shipmentRepository;
    private final DriverRepository driverRepository;
    private final ShipmentService shipmentService;
    private final AuditLogService auditLogService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Phân công tài xế với cơ chế bảo vệ kép:
     * 1. Pessimistic Lock trên Driver để khoá dòng tài xế, tính toán chính xác tải trọng & số đơn đang nhận.
     * 2. Optimistic Lock (@Version) trên Shipment để ngăn 2 nhân viên điều phối cùng gán 1 đơn cùng tích tắc.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ShipmentResponse assignDriver(Long shipmentId, AssignDriverRequest request) {
        log.info("[DISPATCH-ASSIGN] Bắt đầu điều phối: Phân công đơn ID #{} cho tài xế ID #{}", shipmentId, request.getDriverId());

        // 1. Kiểm tra và khoá tài xế (Pessimistic Write Lock)
        Driver driver = driverRepository.findByIdForUpdate(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Tài xế", "id", request.getDriverId()));

        if (driver.getStatus() != DriverStatus.AVAILABLE) {
            throw new BusinessException("Tài xế hiện không ở trạng thái sẵn sàng (Trạng thái hiện tại: " + driver.getStatus() + ")");
        }

        // Kiểm tra số lượng đơn đang phụ trách
        long activeOrders = driverRepository.countActiveOrdersForDriver(driver.getId());
        if (activeOrders >= driver.getMaxActiveOrders()) {
            throw new BusinessException(String.format("Tài xế đã đạt giới hạn tối đa đơn hàng đang xử lý (%d/%d)",
                    activeOrders, driver.getMaxActiveOrders()));
        }

        // 2. Lấy đơn hàng và kiểm tra trạng thái
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn vận chuyển", "id", shipmentId));

        if (shipment.getStatus() != ShipmentStatus.PENDING) {
            throw new BusinessException("Chỉ đơn hàng ở trạng thái PENDING mới có thể phân công. Trạng thái hiện tại: " + shipment.getStatus());
        }

        // Kiểm tra phiên bản Optimistic Lock nếu client truyền lên
        if (request.getExpectedVersion() != null && !Objects.equals(shipment.getVersion(), request.getExpectedVersion())) {
            throw new OptimisticLockingException(String.format(
                    "Xung đột phiên bản: Đơn hàng đã thay đổi (Phiên bản yêu cầu: %d, phiên bản hiện tại: %d). Vui lòng tải lại!",
                    request.getExpectedVersion(), shipment.getVersion()));
        }

        // Kiểm tra tải trọng xe nếu có phương tiện
        if (driver.getVehicle() != null && driver.getVehicle().getMaxPayloadKg() != null) {
            if (shipment.getTotalWeightKg() > driver.getVehicle().getMaxPayloadKg()) {
                throw new BusinessException(String.format(
                        "Khối lượng đơn hàng (%.2f kg) vượt quá tải trọng tối đa của xe (%.2f kg) thuộc tài xế này",
                        shipment.getTotalWeightKg(), driver.getVehicle().getMaxPayloadKg()));
            }
        }

        // Gán tài xế và chuyển trạng thái sang ASSIGNED
        shipment.setAssignedDriver(driver);
        shipment.setStatus(ShipmentStatus.ASSIGNED);

        // Lưu Shipment -> Kích hoạt @Version check tự động của Hibernate
        Shipment updated = shipmentRepository.save(shipment);

        // Lấy thông tin người điều phối
        Long dispatcherId = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            dispatcherId = principal.getId();
        }

        // Ghi Audit Log
        auditLogService.record("ASSIGN_DRIVER", "SHIPMENT", String.valueOf(updated.getId()),
                "{\"status\":\"PENDING\",\"assignedDriver\":null}",
                String.format("{\"status\":\"ASSIGNED\",\"driverId\":%d,\"driverName\":\"%s\"}",
                        driver.getId(), driver.getUser().getFullName()));

        // Bắn domain event
        eventPublisher.publishEvent(ShipmentAssignedEvent.builder()
                .shipmentId(updated.getId())
                .trackingCode(updated.getTrackingCode())
                .driverId(driver.getId())
                .driverName(driver.getUser().getFullName())
                .assignedByUserId(dispatcherId)
                .build());

        log.info("[DISPATCH-ASSIGN-SUCCESS] Phân công thành công Shipment #{} ({}) cho tài xế #{} ({}). Version: {}",
                updated.getId(), updated.getTrackingCode(), driver.getId(), driver.getUser().getFullName(), updated.getVersion());

        return shipmentService.mapToResponse(updated);
    }

    /**
     * Hủy phân công tài xế, đưa đơn hàng về lại trạng thái PENDING.
     */
    @Transactional
    public ShipmentResponse unassignDriver(Long shipmentId) {
        log.info("[DISPATCH-UNASSIGN] Yêu cầu hủy phân công tài xế cho đơn #{}", shipmentId);

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn vận chuyển", "id", shipmentId));

        if (shipment.getStatus() != ShipmentStatus.ASSIGNED) {
            log.warn("[DISPATCH-UNASSIGN-FAIL] Đơn #{} không ở trạng thái ASSIGNED (Hiện tại: {})", shipmentId, shipment.getStatus());
            throw new BusinessException("Chỉ có thể hủy phân công khi đơn đang ở trạng thái ASSIGNED");
        }

        Long previousDriverId = shipment.getAssignedDriver() != null ? shipment.getAssignedDriver().getId() : null;
        shipment.setAssignedDriver(null);
        shipment.setStatus(ShipmentStatus.PENDING);

        Shipment updated = shipmentRepository.save(shipment);

        auditLogService.record("UNASSIGN_DRIVER", "SHIPMENT", String.valueOf(updated.getId()),
                String.format("{\"assignedDriverId\":%s}", previousDriverId),
                "{\"assignedDriverId\":null,\"status\":\"PENDING\"}");

        log.info("[DISPATCH-UNASSIGN-SUCCESS] Đã hủy phân công tài xế #{} cho đơn #{}. Trạng thái trở về PENDING", previousDriverId, shipmentId);
        return shipmentService.mapToResponse(updated);
    }
}

