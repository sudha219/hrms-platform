package com.hrms.dto.attendance;

import com.hrms.enums.AttendanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class AttendanceCorrectionRequest {

    @NotNull(message = "Check-in time is required")
    private Instant checkIn;

    private Instant checkOut;

    private AttendanceStatus status;

    @NotBlank(message = "Correction reason is mandatory for audit trail")
    private String reason;
}
