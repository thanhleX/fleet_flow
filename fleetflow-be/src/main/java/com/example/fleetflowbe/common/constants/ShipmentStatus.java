package com.example.fleetflowbe.common.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ShipmentStatus {
    PENDING,            // Chờ xử lý / Chưa phân công
    ASSIGNED,           // Đã phân công tài xế
    PICKED_UP,          // Đã lấy hàng
    AT_ORIGIN_HUB,      // Hàng tại kho gốc
    IN_TRANSIT,         // Đang trung chuyển
    AT_DEST_HUB,        // Hàng tại kho đích
    OUT_FOR_DELIVERY,    // Đang giao hàng
    DELIVERED,          // Giao hàng thành công
    DELIVERY_FAILED,     // Giao hàng thất bại
    RETURNED,           // Đã hoàn trả hàng về người gửi
    CANCELLED;          // Đơn bị hủy

    @JsonValue
    public String getValue() {
        return this.name();
    }

    public boolean canTransitionTo(ShipmentStatus next) {
        if (next == null) return false;
        return switch (this) {
            case PENDING -> next == ASSIGNED || next == CANCELLED;
            case ASSIGNED -> next == PICKED_UP || next == PENDING || next == CANCELLED;
            case PICKED_UP -> next == AT_ORIGIN_HUB || next == IN_TRANSIT || next == OUT_FOR_DELIVERY;
            case AT_ORIGIN_HUB -> next == IN_TRANSIT || next == OUT_FOR_DELIVERY;
            case IN_TRANSIT -> next == AT_DEST_HUB || next == OUT_FOR_DELIVERY || next == IN_TRANSIT;
            case AT_DEST_HUB -> next == OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> next == DELIVERED || next == DELIVERY_FAILED;
            case DELIVERY_FAILED -> next == OUT_FOR_DELIVERY || next == RETURNED;
            case RETURNED, DELIVERED, CANCELLED -> false; // Terminal states
        };
    }

    @JsonCreator
    public static ShipmentStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return PENDING;
        }
        String normalized = value.trim().toUpperCase();

        // Tự động chuẩn hóa các trạng thái cũ/vụn vặt về trạng thái chuẩn
        switch (normalized) {
            case "PICKING_UP" -> {
                return ASSIGNED;
            }
            case "RESCHEDULED" -> {
                return DELIVERY_FAILED;
            }
            case "RETURNING" -> {
                return RETURNED;
            }
        }

        for (ShipmentStatus s : values()) {
            if (s.name().equalsIgnoreCase(normalized)) {
                return s;
            }
        }
        return PENDING;
    }
}
