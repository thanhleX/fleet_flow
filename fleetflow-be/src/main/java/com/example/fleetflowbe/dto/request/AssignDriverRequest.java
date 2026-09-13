package com.example.fleetflowbe.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignDriverRequest {

    @NotNull(message = "ID tài xế không được để trống")
    private Long driverId;

    /**
     * Optional expected entity version from client for optimistic concurrency check
     */
    private Long expectedVersion;
}

