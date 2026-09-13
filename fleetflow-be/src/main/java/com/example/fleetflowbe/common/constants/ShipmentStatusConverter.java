package com.example.fleetflowbe.common.constants;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter(autoApply = true)
public class ShipmentStatusConverter implements AttributeConverter<ShipmentStatus, String> {

    @Override
    public String convertToDatabaseColumn(ShipmentStatus status) {
        if (status == null) {
            return ShipmentStatus.PENDING.name();
        }
        return status.name();
    }

    @Override
    public ShipmentStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            log.warn("[SHIPMENT-STATUS-CONVERTER] Status null/rỗng trong DB, tự động quy về PENDING");
            return ShipmentStatus.PENDING;
        }
        return ShipmentStatus.fromString(dbData);
    }
}

