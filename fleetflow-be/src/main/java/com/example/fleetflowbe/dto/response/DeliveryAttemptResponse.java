package com.example.fleetflowbe.dto.response;

import com.example.fleetflowbe.common.constants.DeliveryAttemptStatus;
import com.example.fleetflowbe.common.constants.FailureReason;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAttemptResponse {
    private Long id;
    private Integer attemptNumber;
    private DeliveryAttemptStatus status;
    private FailureReason failureReason;
    private String failureNote;
    private String proofImageUrl;
    private Long driverId;
    private String driverName;
    private LocalDateTime attemptedAt;
    private LocalDateTime rescheduledDate;
}

