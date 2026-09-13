package com.example.fleetflowbe.security;

import com.example.fleetflowbe.common.constants.Role;
import com.example.fleetflowbe.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("securityEvaluator")
@RequiredArgsConstructor
public class SecurityEvaluator {

    private final ShipmentRepository shipmentRepository;

    /**
     * Check if the authenticated driver is the one assigned to this shipment.
     */
    public boolean isDriverAssigned(Long shipmentId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal userPrincipal)) {
            return false;
        }

        // Admin and Managers have overarching access
        if (userPrincipal.getRole() == Role.ROLE_ADMIN || userPrincipal.getRole() == Role.ROLE_MANAGER) {
            return true;
        }

        if (userPrincipal.getDriverId() == null) {
            return false;
        }

        return shipmentRepository.findById(shipmentId)
                .map(shipment -> shipment.getAssignedDriver() != null &&
                        shipment.getAssignedDriver().getId().equals(userPrincipal.getDriverId()))
                .orElse(false);
    }

    /**
     * Check if the authenticated driver is acting on their own driver record.
     */
    public boolean isOwnDriverProfile(Long driverId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal userPrincipal)) {
            return false;
        }

        if (userPrincipal.getRole() == Role.ROLE_ADMIN || userPrincipal.getRole() == Role.ROLE_MANAGER) {
            return true;
        }

        return userPrincipal.getDriverId() != null && userPrincipal.getDriverId().equals(driverId);
    }
}

