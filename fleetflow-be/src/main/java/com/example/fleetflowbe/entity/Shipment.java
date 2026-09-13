package com.example.fleetflowbe.entity;

import com.example.fleetflowbe.common.constants.PaymentStatus;
import com.example.fleetflowbe.common.constants.ServiceTier;
import com.example.fleetflowbe.common.constants.ShipmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shipments", indexes = {
        @Index(name = "idx_shipment_tracking", columnList = "tracking_code", unique = true),
        @Index(name = "idx_shipment_status", columnList = "status"),
        @Index(name = "idx_shipment_driver", columnList = "assigned_driver_id"),
        @Index(name = "idx_shipment_origin_hub", columnList = "origin_hub_id"),
        @Index(name = "idx_shipment_dest_hub", columnList = "destination_hub_id"),
        @Index(name = "idx_shipment_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment extends BaseEntity {

    @Column(name = "tracking_code", nullable = false, unique = true, length = 32)
    private String trackingCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_customer_id")
    private Customer senderCustomer;

    @Column(name = "sender_name", nullable = false, length = 100)
    private String senderName;

    @Column(name = "sender_phone", nullable = false, length = 20)
    private String senderPhone;

    @Column(name = "sender_address", nullable = false, length = 255)
    private String senderAddress;

    @Column(name = "sender_province", length = 50)
    private String senderProvince;

    @Column(name = "receiver_name", nullable = false, length = 100)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false, length = 20)
    private String receiverPhone;

    @Column(name = "receiver_address", nullable = false, length = 255)
    private String receiverAddress;

    @Column(name = "receiver_province", length = 50)
    private String receiverProvince;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_hub_id", nullable = false)
    private Warehouse originHub;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_hub_id")
    private Warehouse destinationHub;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_driver_id")
    private Driver assignedDriver;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "service_tier", nullable = false, length = 20)
    private ServiceTier serviceTier = ServiceTier.STANDARD;

    @Column(name = "total_weight_kg", nullable = false)
    private Double totalWeightKg;

    @Column(name = "volumetric_weight_kg")
    private Double volumetricWeightKg;

    @Column(name = "declared_value", precision = 12, scale = 2)
    private BigDecimal declaredValue;

    @Column(name = "shipping_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingFee;

    @Builder.Default
    @Column(name = "cod_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal codAmount = BigDecimal.ZERO;

    @Convert(converter = com.example.fleetflowbe.common.constants.PaymentStatusConverter.class)
    @Builder.Default
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Convert(converter = com.example.fleetflowbe.common.constants.ShipmentStatusConverter.class)
    @Builder.Default
    @Column(nullable = false, length = 25)
    private ShipmentStatus status = ShipmentStatus.PENDING;

    @Builder.Default
    @Column(name = "delivery_attempt_count", nullable = false)
    private Integer deliveryAttemptCount = 0;

    @Column(length = 255)
    private String note;

    /**
     * Optimistic locking version field.
     * Prevents lost updates and concurrent assignment conflicts.
     */
    @Version
    @Column(name = "version")
    private Long version;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ShipmentItem> items = new ArrayList<>();

    public void addItem(ShipmentItem item) {
        items.add(item);
        item.setShipment(this);
    }
}

