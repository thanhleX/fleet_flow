package com.example.fleetflowbe.common.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Role {
    ROLE_ADMIN,
    ROLE_MANAGER,
    ROLE_STAFF,
    ROLE_DRIVER,
    // Aliases to gracefully support non-prefixed DB values or external payloads
    ADMIN,
    MANAGER,
    STAFF,
    DRIVER;

    @JsonValue
    public String getValue() {
        return this.toCanonical().name();
    }

    public Role toCanonical() {
        return switch (this) {
            case ADMIN, ROLE_ADMIN -> ROLE_ADMIN;
            case MANAGER, ROLE_MANAGER -> ROLE_MANAGER;
            case STAFF, ROLE_STAFF -> ROLE_STAFF;
            case DRIVER, ROLE_DRIVER -> ROLE_DRIVER;
        };
    }

    public boolean isAdmin() {
        return this == ROLE_ADMIN || this == ADMIN;
    }

    public boolean isManager() {
        return this == ROLE_MANAGER || this == MANAGER;
    }

    public boolean isStaff() {
        return this == ROLE_STAFF || this == STAFF;
    }

    public boolean isDriver() {
        return this == ROLE_DRIVER || this == DRIVER;
    }

    @JsonCreator
    public static Role fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return ROLE_STAFF;
        }
        String normalized = value.trim().toUpperCase();

        for (Role r : Role.values()) {
            if (r.name().equalsIgnoreCase(normalized)) {
                return r.toCanonical();
            }
        }

        if (normalized.startsWith("ROLE_")) {
            String stripped = normalized.substring(5);
            for (Role r : Role.values()) {
                if (r.name().equalsIgnoreCase(stripped)) {
                    return r.toCanonical();
                }
            }
        } else {
            String prefixed = "ROLE_" + normalized;
            for (Role r : Role.values()) {
                if (r.name().equalsIgnoreCase(prefixed)) {
                    return r.toCanonical();
                }
            }
        }

        return ROLE_STAFF;
    }
}
