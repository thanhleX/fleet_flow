package com.example.fleetflowbe.entity;

import com.example.fleetflowbe.common.constants.DeliveryAttemptStatus;
import com.example.fleetflowbe.common.constants.FailureReason;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_attempts", indexes = {
        @Index(name = "idx_attempt_shipment", columnList = "shipment_id"),
        @Index(name = "idx_attempt_driver", columnList = "driver_id"),
        @Index(name = "idx_attempt_time", columnList = "attempted_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryAttemptStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "failure_reason", length = 30)
    private FailureReason failureReason;

    @Column(name = "failure_note", length = 255)
    private String failureNote;

    @Column(name = "proof_image_url", length = 500)
    private String proofImageUrl;

    @Column(name = "attempted_at", nullable = false)
    private LocalDateTime attemptedAt;

    @Column(name = "rescheduled_date")
    private LocalDateTime rescheduledDate;
}

