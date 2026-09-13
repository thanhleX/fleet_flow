package com.example.fleetflowbe.service;

import com.example.fleetflowbe.common.constants.CodStatus;
import com.example.fleetflowbe.common.constants.SettlementStatus;
import com.example.fleetflowbe.common.exception.BusinessException;
import com.example.fleetflowbe.common.exception.ResourceNotFoundException;
import com.example.fleetflowbe.dto.request.CodSettlementRequest;
import com.example.fleetflowbe.dto.response.CodSettlementResponse;
import com.example.fleetflowbe.dto.response.CodTransactionResponse;
import com.example.fleetflowbe.entity.CodSettlement;
import com.example.fleetflowbe.entity.CodTransaction;
import com.example.fleetflowbe.entity.Driver;
import com.example.fleetflowbe.entity.User;
import com.example.fleetflowbe.repository.CodSettlementRepository;
import com.example.fleetflowbe.repository.CodTransactionRepository;
import com.example.fleetflowbe.repository.DriverRepository;
import com.example.fleetflowbe.repository.UserRepository;
import com.example.fleetflowbe.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodSettlementService {

    private final CodSettlementRepository codSettlementRepository;
    private final CodTransactionRepository codTransactionRepository;
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    /**
     * Thực hiện đối soát tiền COD nộp về với cơ chế IDEMPOTENCY triệt để:
     * Nếu Idempotency-Key đã tồn tại, trả về kết quả đối soát cũ mà không thực hiện trừ tiền / cộng quỹ lần 2.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CodSettlementResponse settleCodForDriver(CodSettlementRequest request) {
        log.info("[COD-SETTLE-REQUEST] Bắt đầu đối soát COD cho tài xế #{}, IdempotencyKey='{}'",
                request.getDriverId(), request.getIdempotencyKey());

        // 1. Kiểm tra Idempotency Key
        Optional<CodSettlement> existingOpt = codSettlementRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existingOpt.isPresent()) {
            log.warn("[COD-IDEMPOTENCY-HIT] Request đối soát với Idempotency-Key '{}' đã xử lý trước đó. Trả về kết quả đã lưu.",
                    request.getIdempotencyKey());
            return mapToResponse(existingOpt.get());
        }

        // 2. Tìm tài xế
        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Tài xế", "id", request.getDriverId()));

        // 3. Lấy tất cả các giao dịch COD đang ở trạng thái COLLECTED của tài xế này
        List<CodTransaction> collectedTxs = codTransactionRepository.findByDriverIdAndStatus(driver.getId(), CodStatus.COLLECTED);

        if (collectedTxs.isEmpty()) {
            throw new BusinessException("Tài xế không có giao dịch tiền COD nào đang chờ đối soát (COLLECTED)");
        }

        BigDecimal totalAmount = collectedTxs.stream()
                .map(CodTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Lấy thông tin nhân viên duyệt đối soát
        User staffUser = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            staffUser = userRepository.findById(principal.getId()).orElse(null);
        }

        String settlementCode = "SETTLE-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        CodSettlement settlement = CodSettlement.builder()
                .settlementCode(settlementCode)
                .idempotencyKey(request.getIdempotencyKey())
                .driver(driver)
                .verifiedByStaff(staffUser)
                .totalAmount(totalAmount)
                .totalOrders(collectedTxs.size())
                .status(SettlementStatus.COMPLETED)
                .settledAt(LocalDateTime.now())
                .note(request.getNote())
                .build();

        CodSettlement savedSettlement = codSettlementRepository.save(settlement);

        // Cập nhật trạng thái từng giao dịch sang VERIFIED và liên kết vào đợt đối soát
        for (CodTransaction tx : collectedTxs) {
            tx.setStatus(CodStatus.VERIFIED);
            tx.setSettlement(savedSettlement);
            tx.setVerifiedAt(LocalDateTime.now());
            codTransactionRepository.save(tx);
        }

        auditLogService.record("SETTLE_COD", "COD_SETTLEMENT", String.valueOf(savedSettlement.getId()),
                null,
                String.format("{\"settlementCode\":\"%s\",\"idempotencyKey\":\"%s\",\"totalAmount\":%s,\"totalOrders\":%d}",
                        settlementCode, request.getIdempotencyKey(), totalAmount, collectedTxs.size()));

        log.info("Đối soát COD thành công: Mã {}, IdempotencyKey {}, Tổng tiền: {}, Số đơn: {}",
                settlementCode, request.getIdempotencyKey(), totalAmount, collectedTxs.size());

        savedSettlement.setTransactions(collectedTxs);
        return mapToResponse(savedSettlement);
    }

    @Transactional(readOnly = true)
    public Page<CodSettlementResponse> getSettlementsByDriver(Long driverId, Pageable pageable) {
        return codSettlementRepository.findByDriverId(driverId, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public CodSettlementResponse getSettlementByCode(String code) {
        CodSettlement settlement = codSettlementRepository.findBySettlementCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Bản đối soát COD", "settlementCode", code));
        return mapToResponse(settlement);
    }

    @Transactional(readOnly = true)
    public List<CodTransactionResponse> getPendingCodForDriver(Long driverId) {
        return codTransactionRepository.findByDriverIdAndStatus(driverId, CodStatus.COLLECTED)
                .stream()
                .map(this::mapTransactionToResponse)
                .toList();
    }

    public CodSettlementResponse mapToResponse(CodSettlement s) {
        List<CodTransactionResponse> txDtos = null;
        if (s.getTransactions() != null) {
            txDtos = s.getTransactions().stream().map(this::mapTransactionToResponse).toList();
        }

        return CodSettlementResponse.builder()
                .id(s.getId())
                .settlementCode(s.getSettlementCode())
                .idempotencyKey(s.getIdempotencyKey())
                .driverId(s.getDriver() != null ? s.getDriver().getId() : null)
                .driverName(s.getDriver() != null && s.getDriver().getUser() != null ?
                        s.getDriver().getUser().getFullName() : null)
                .verifiedByStaffId(s.getVerifiedByStaff() != null ? s.getVerifiedByStaff().getId() : null)
                .verifiedByStaffName(s.getVerifiedByStaff() != null ? s.getVerifiedByStaff().getFullName() : null)
                .totalAmount(s.getTotalAmount())
                .totalOrders(s.getTotalOrders())
                .status(s.getStatus())
                .settledAt(s.getSettledAt())
                .note(s.getNote())
                .transactions(txDtos)
                .build();
    }

    private CodTransactionResponse mapTransactionToResponse(CodTransaction tx) {
        return CodTransactionResponse.builder()
                .id(tx.getId())
                .shipmentId(tx.getShipment() != null ? tx.getShipment().getId() : null)
                .trackingCode(tx.getShipment() != null ? tx.getShipment().getTrackingCode() : null)
                .amount(tx.getAmount())
                .status(tx.getStatus())
                .driverId(tx.getDriver() != null ? tx.getDriver().getId() : null)
                .driverName(tx.getDriver() != null && tx.getDriver().getUser() != null ?
                        tx.getDriver().getUser().getFullName() : null)
                .settlementId(tx.getSettlement() != null ? tx.getSettlement().getId() : null)
                .collectedAt(tx.getCollectedAt())
                .submittedAt(tx.getSubmittedAt())
                .verifiedAt(tx.getVerifiedAt())
                .disputeReason(tx.getDisputeReason())
                .build();
    }
}

