package com.example.fleetflowbe.common.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentStatus {
    PENDING,    // Chưa thanh toán / Chờ thu COD
    PAID,       // Đã thanh toán
    REFUNDED,   // Đã hoàn tiền
    
    // Hỗ trợ tương thích ngược cho dữ liệu cũ từ DB
    PAID_BY_SENDER,
    PAID_BY_RECEIVER,
    SETTLED;

    @JsonValue
    public String getValue() {
        return this.toCanonical().name();
    }

    public PaymentStatus toCanonical() {
        return switch (this) {
            case PENDING -> PENDING;
            case PAID, PAID_BY_SENDER, PAID_BY_RECEIVER, SETTLED -> PAID;
            case REFUNDED -> REFUNDED;
        };
    }

    @JsonCreator
    public static PaymentStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return PENDING;
        }
        String normalized = value.trim().toUpperCase();
        for (PaymentStatus p : values()) {
            if (p.name().equalsIgnoreCase(normalized)) {
                return p.toCanonical();
            }
        }
        return PENDING;
    }
}
