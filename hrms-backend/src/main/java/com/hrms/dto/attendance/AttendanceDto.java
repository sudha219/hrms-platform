package com.hrms.dto.attendance;

import com.hrms.enums.AttendanceStatus;
import com.hrms.enums.SourceChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDto {
    private UUID id;
    private UUID employeeId;
    private String employeeName;
    private String employeeCode;
    private LocalDate date;
    private Instant checkIn;
    private Instant checkOut;
    private Integer totalMinutes;
    private AttendanceStatus status;
    private SourceChannel sourceChannel;
    private String correctionReason;
}
