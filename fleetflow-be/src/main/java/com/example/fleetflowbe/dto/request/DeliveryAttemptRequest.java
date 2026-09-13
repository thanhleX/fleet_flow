package com.example.fleetflowbe.dto.request;

import com.example.fleetflowbe.common.constants.DeliveryAttemptStatus;
import com.example.fleetflowbe.common.constants.FailureReason;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAttemptRequest {

    @NotNull(message = "Kết quả lần giao không được để trống")
    private DeliveryAttemptStatus status;

    private FailureReason failureReason;

    private String failureNote;

    private String proofImageUrl;

    private LocalDateTime rescheduledDate;
}

