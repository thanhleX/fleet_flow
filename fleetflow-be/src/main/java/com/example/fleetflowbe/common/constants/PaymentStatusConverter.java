package com.example.fleetflowbe.common.constants;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter(autoApply = true)
public class PaymentStatusConverter implements AttributeConverter<PaymentStatus, String> {

    @Override
    public String convertToDatabaseColumn(PaymentStatus attribute) {
        if (attribute == null) {
            return PaymentStatus.PENDING.name();
        }
        return attribute.toCanonical().name();
    }

    @Override
    public PaymentStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return PaymentStatus.PENDING;
        }
        return PaymentStatus.fromString(dbData);
    }
}

