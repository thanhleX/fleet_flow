package com.example.fleetflowbe.common.constants;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Role, String> {

    @Override
    public String convertToDatabaseColumn(Role role) {
        if (role == null) {
            return Role.ROLE_STAFF.name();
        }
        return role.toCanonical().name();
    }

    @Override
    public Role convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            log.warn("[ROLE-CONVERTER] CSDL chứa role null/rỗng, tự động quy về ROLE_STAFF");
            return Role.ROLE_STAFF;
        }
        return Role.fromString(dbData);
    }
}

