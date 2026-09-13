package com.example.fleetflowbe.service;

import com.example.fleetflowbe.common.constants.CodStatus;
import com.example.fleetflowbe.common.constants.RegionZone;
import com.example.fleetflowbe.common.constants.ShipmentStatus;
import com.example.fleetflowbe.common.exception.InvalidStateTransitionException;
import com.example.fleetflowbe.common.exception.ResourceNotFoundException;
import com.example.fleetflowbe.dto.request.CreateShipmentRequest;
import com.example.fleetflowbe.dto.request.PricingCalculateRequest;
import com.example.fleetflowbe.dto.request.UpdateShipmentStatusRequest;
import com.example.fleetflowbe.dto.response.PricingEstimateResponse;
import com.example.fleetflowbe.dto.response.ShipmentResponse;
import com.example.fleetflowbe.entity.*;
import com.example.fleetflowbe.event.ShipmentStatusChangedEvent;
import com.example.fleetflowbe.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final CustomerRepository customerRepository;
    private final WarehouseRepository warehouseRepository;
    private final CodTransactionRepository codTransactionRepository;
    private final PricingEngineService pricingEngineService;
    private final AuditLogService auditLogService;
    private final ApplicationEventPublisher eventPublisher;

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    public ShipmentResponse createShipment(CreateShipmentRequest request) {
        Warehouse originHub = warehouseRepository.findById(request.getOriginHubId())
                .orElseThrow(() -> new ResourceNotFoundException("Kho gửi (originHub)", "id", request.getOriginHubId()));

        Warehouse destHub = null;
        if (request.getDestinationHubId() != null) {
            destHub = warehouseRepository.findById(request.getDestinationHubId())
                    .orElseThrow(() -> new ResourceNotFoundException("Kho nhận (destinationHub)", "id", request.getDestinationHubId()));
        }

        Customer senderCustomer = null;
        if (request.getSenderCustomerId() != null) {
            senderCustomer = customerRepository.findById(request.getSenderCustomerId()).orElse(null);
        }

        // Tự động tính phí nếu cần
        RegionZone zone = RegionZone.INTRA_PROVINCE;
        if (destHub != null && !originHub.getProvince().equalsIgnoreCase(destHub.getProvince())) {
            zone = RegionZone.INTER_PROVINCE;
        }

        PricingCalculateRequest pricingReq = PricingCalculateRequest.builder()
                .regionZone(zone)
                .serviceTier(request.getServiceTier())
                .weightKg(request.getTotalWeightKg())
                .lengthCm(request.getLengthCm())
                .widthCm(request.getWidthCm())
                .heightCm(request.getHeightCm())
                .declaredValue(request.getDeclaredValue())
                .codAmount(request.getCodAmount())
                .build();
        PricingEstimateResponse pricing = pricingEngineService.estimateShippingFee(pricingReq);

        String trackingCode = generateUniqueTrackingCode();

        Shipment shipment = Shipment.builder()
                .trackingCode(trackingCode)
                .senderCustomer(senderCustomer)
                .senderName(request.getSenderName())
                .senderPhone(request.getSenderPhone())
                .senderAddress(request.getSenderAddress())
                .senderProvince(request.getSenderProvince() != null ? request.getSenderProvince() : originHub.getProvince())
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .receiverAddress(request.getReceiverAddress())
                .receiverProvince(request.getReceiverProvince())
                .originHub(originHub)
                .destinationHub(destHub)
                .serviceTier(request.getServiceTier())
                .totalWeightKg(request.getTotalWeightKg())
                .volumetricWeightKg(pricing.getVolumetricWeightKg())
                .declaredValue(request.getDeclaredValue())
                .shippingFee(pricing.getTotalEstimatedFee())
                .codAmount(request.getCodAmount() != null ? request.getCodAmount() : BigDecimal.ZERO)
                .paymentStatus(request.getPaymentStatus())
                .status(ShipmentStatus.PENDING)
                .deliveryAttemptCount(0)
                .note(request.getNote())
                .build();

        // Gán chi tiết mặt hàng
        if (request.getItems() != null) {
            for (var itemReq : request.getItems()) {
                ShipmentItem item = ShipmentItem.builder()
                        .itemName(itemReq.getItemName())
                        .quantity(itemReq.getQuantity())
                        .weightKg(itemReq.getWeightKg())
                        .declaredPrice(itemReq.getDeclaredPrice())
                        .build();
                shipment.addItem(item);
            }
        }

        Shipment savedShipment = shipmentRepository.save(shipment);

        // Khởi tạo COD transaction nếu có tiền thu hộ
        if (savedShipment.getCodAmount().compareTo(BigDecimal.ZERO) > 0) {
            CodTransaction codTransaction = CodTransaction.builder()
                    .shipment(savedShipment)
                    .amount(savedShipment.getCodAmount())
                    .status(CodStatus.PENDING)
                    .build();
            codTransactionRepository.save(codTransaction);
        }

        auditLogService.record("CREATE_SHIPMENT", "SHIPMENT", String.valueOf(savedShipment.getId()),
                null, String.format("{\"trackingCode\":\"%s\",\"fee\":%s}", trackingCode, savedShipment.getShippingFee()));

        log.info("[SHIPMENT-CREATED] Tạo mới đơn hàng thành công: ID={}, TrackingCode='{}', Phí cước={} VNĐ, COD={} VNĐ",
                savedShipment.getId(), trackingCode, savedShipment.getShippingFee(), savedShipment.getCodAmount());
        return mapToResponse(savedShipment);
    }

    @Transactional
    public ShipmentResponse updateStatus(Long shipmentId, UpdateShipmentStatusRequest request) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn vận chuyển", "id", shipmentId));

        ShipmentStatus currentStatus = shipment.getStatus();
        ShipmentStatus targetStatus = request.getStatus();

        log.info("[SHIPMENT-STATUS-TRANSITION] Yêu cầu chuyển trạng thái đơn #{}: {} -> {} (Ghi chú: '{}')",
                shipmentId, currentStatus, targetStatus, request.getNote());

        if (!currentStatus.canTransitionTo(targetStatus)) {
            log.warn("[SHIPMENT-STATUS-INVALID] Chuyển trạng thái bất hợp lệ cho đơn #{}: {} -> {}",
                    shipmentId, currentStatus, targetStatus);
            throw new InvalidStateTransitionException(currentStatus.name(), targetStatus.name());
        }

        shipment.setStatus(targetStatus);
        if (request.getNote() != null) {
            shipment.setNote(request.getNote());
        }

        Shipment updated = shipmentRepository.save(shipment);

        auditLogService.record("UPDATE_STATUS", "SHIPMENT", String.valueOf(updated.getId()),
                String.format("{\"status\":\"%s\"}", currentStatus),
                String.format("{\"status\":\"%s\",\"note\":\"%s\"}", targetStatus, request.getNote()));

        eventPublisher.publishEvent(ShipmentStatusChangedEvent.builder()
                .shipmentId(updated.getId())
                .trackingCode(updated.getTrackingCode())
                .oldStatus(currentStatus)
                .newStatus(targetStatus)
                .note(request.getNote())
                .build());

        log.info("[SHIPMENT-STATUS-SUCCESS] Chuyển trạng thái đơn #{} thành công: {}", shipmentId, targetStatus);
        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentById(Long id) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn vận chuyển", "id", id));
        return mapToResponse(shipment);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentByTrackingCode(String code) {
        Shipment shipment = shipmentRepository.findByTrackingCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn vận chuyển", "trackingCode", code));
        return mapToResponse(shipment);
    }

    @Transactional(readOnly = true)
    public Page<ShipmentResponse> getShipmentsByStatus(ShipmentStatus status, Pageable pageable) {
        return shipmentRepository.findByStatus(status, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ShipmentResponse> getShipmentsByDriver(Long driverId, Pageable pageable) {
        return shipmentRepository.findByAssignedDriverId(driverId, pageable).map(this::mapToResponse);
    }

    public ShipmentResponse mapToResponse(Shipment s) {
        return ShipmentResponse.builder()
                .id(s.getId())
                .trackingCode(s.getTrackingCode())
                .senderName(s.getSenderName())
                .senderPhone(s.getSenderPhone())
                .senderAddress(s.getSenderAddress())
                .receiverName(s.getReceiverName())
                .receiverPhone(s.getReceiverPhone())
                .receiverAddress(s.getReceiverAddress())
                .originHubId(s.getOriginHub() != null ? s.getOriginHub().getId() : null)
                .originHubName(s.getOriginHub() != null ? s.getOriginHub().getName() : null)
                .destinationHubId(s.getDestinationHub() != null ? s.getDestinationHub().getId() : null)
                .destinationHubName(s.getDestinationHub() != null ? s.getDestinationHub().getName() : null)
                .assignedDriverId(s.getAssignedDriver() != null ? s.getAssignedDriver().getId() : null)
                .assignedDriverName(s.getAssignedDriver() != null && s.getAssignedDriver().getUser() != null ?
                        s.getAssignedDriver().getUser().getFullName() : null)
                .serviceTier(s.getServiceTier())
                .totalWeightKg(s.getTotalWeightKg())
                .volumetricWeightKg(s.getVolumetricWeightKg())
                .declaredValue(s.getDeclaredValue())
                .shippingFee(s.getShippingFee())
                .codAmount(s.getCodAmount())
                .paymentStatus(s.getPaymentStatus())
                .status(s.getStatus())
                .deliveryAttemptCount(s.getDeliveryAttemptCount())
                .version(s.getVersion())
                .note(s.getNote())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .items(s.getItems() != null ? s.getItems().stream().map(i -> ShipmentResponse.ItemDto.builder()
                        .id(i.getId())
                        .itemName(i.getItemName())
                        .quantity(i.getQuantity())
                        .weightKg(i.getWeightKg())
                        .declaredPrice(i.getDeclaredPrice())
                        .build()).toList() : List.of())
                .build();
    }

    private String generateUniqueTrackingCode() {
        StringBuilder sb = new StringBuilder("FF");
        for (int i = 0; i < 8; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        String code = sb.toString();
        if (shipmentRepository.existsByTrackingCode(code)) {
            return generateUniqueTrackingCode();
        }
        return code;
    }
}

