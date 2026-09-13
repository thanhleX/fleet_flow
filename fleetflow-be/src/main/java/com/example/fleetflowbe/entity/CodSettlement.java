package com.example.fleetflowbe.entity;

import com.example.fleetflowbe.common.constants.SettlementStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cod_settlements", indexes = {
        @Index(name = "idx_settlement_idempotency", columnList = "idempotency_key", unique = true),
        @Index(name = "idx_settlement_code", columnList = "settlement_code", unique = true),
        @Index(name = "idx_settlement_driver", columnList = "driver_id"),
        @Index(name = "idx_settlement_date", columnList = "settled_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodSettlement extends BaseEntity {

    @Column(name = "settlement_code", nullable = false, unique = true, length = 50)
    private String settlementCode;

    /**
     * Idempotency key sent from Client/API header.
     * Prevents duplicate double-settlement requests.
     */
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_staff_id")
    private User verifiedByStaff;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "total_orders", nullable = false)
    private Integer totalOrders;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private SettlementStatus status = SettlementStatus.PENDING;

    @Column(name = "settled_at", nullable = false)
    private LocalDateTime settledAt;

    @Column(length = 255)
    private String note;

    @OneToMany(mappedBy = "settlement")
    @Builder.Default
    private List<CodTransaction> transactions = new ArrayList<>();
}

