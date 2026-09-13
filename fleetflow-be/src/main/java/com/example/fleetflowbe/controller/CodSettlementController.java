package com.example.fleetflowbe.controller;

import com.example.fleetflowbe.common.response.ApiResponse;
import com.example.fleetflowbe.common.response.PageResponse;
import com.example.fleetflowbe.dto.request.CodSettlementRequest;
import com.example.fleetflowbe.dto.response.CodSettlementResponse;
import com.example.fleetflowbe.dto.response.CodTransactionResponse;
import com.example.fleetflowbe.service.CodSettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "COD & Settlement", description = "Quản lý dòng tiền thu hộ COD và đối soát an toàn với Idempotency Key")
@RestController
@RequestMapping("/api/v1/cod")
@RequiredArgsConstructor
public class CodSettlementController {

    private final CodSettlementService codSettlementService;

    @Operation(summary = "Xác nhận đối soát tiền COD cho tài xế (Hỗ trợ Idempotency)")
    @PostMapping("/settle")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<CodSettlementResponse>> settleCod(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKeyHeader,
            @Valid @RequestBody CodSettlementRequest request) {

        if (StringUtils.hasText(idempotencyKeyHeader)) {
            request.setIdempotencyKey(idempotencyKeyHeader);
        }

        CodSettlementResponse response = codSettlementService.settleCodForDriver(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Đối soát tiền COD thành công"));
    }

    @Operation(summary = "Xem danh sách các đơn COD tài xế đã thu nhưng chưa đối soát")
    @GetMapping("/driver/{driverId}/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF') or (hasRole('DRIVER') and @securityEvaluator.isOwnDriverProfile(#driverId, authentication))")
    public ResponseEntity<ApiResponse<List<CodTransactionResponse>>> getPendingCod(@PathVariable Long driverId) {
        List<CodTransactionResponse> list = codSettlementService.getPendingCodForDriver(driverId);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @Operation(summary = "Xem chi tiết một đợt đối soát theo mã")
    @GetMapping("/settlements/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<CodSettlementResponse>> getSettlementByCode(@PathVariable String code) {
        CodSettlementResponse response = codSettlementService.getSettlementByCode(code);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Xem lịch sử các đợt đối soát của tài xế")
    @GetMapping("/driver/{driverId}/settlements")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF') or (hasRole('DRIVER') and @securityEvaluator.isOwnDriverProfile(#driverId, authentication))")
    public ResponseEntity<ApiResponse<PageResponse<CodSettlementResponse>>> getSettlementsByDriver(
            @PathVariable Long driverId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var result = codSettlementService.getSettlementsByDriver(driverId,
                PageRequest.of(page, size, Sort.by("settledAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(result)));
    }
}

