package com.example.fleetflowbe.entity;

import com.example.fleetflowbe.common.constants.CodStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cod_transactions", indexes = {
        @Index(name = "idx_cod_shipment", columnList = "shipment_id", unique = true),
        @Index(name = "idx_cod_driver", columnList = "driver_id"),
        @Index(name = "idx_cod_status", columnList = "status"),
        @Index(name = "idx_cod_settlement", columnList = "settlement_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodTransaction extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false, unique = true)
    private Shipment shipment;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private CodStatus status = CodStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id")
    private CodSettlement settlement;

    @Column(name = "collected_at")
    private LocalDateTime collectedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "dispute_reason", length = 255)
    private String disputeReason;
}

