package com.hrms.controller;

import com.hrms.common.ApiResponse;
import com.hrms.dto.attendance.AttendanceCorrectionRequest;
import com.hrms.dto.attendance.AttendanceDto;
import com.hrms.dto.attendance.CheckInRequest;
import com.hrms.security.UserPrincipal;
import com.hrms.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance Management", description = "Endpoints for clock-in/out, logs, and HR corrections")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check-in")
    @Operation(summary = "Clock in for the day (Web or Mobile)")
    public ResponseEntity<ApiResponse<AttendanceDto>> checkIn(
            @RequestBody(required = false) CheckInRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (request == null) request = new CheckInRequest();
        AttendanceDto dto = attendanceService.checkIn(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok(dto, "Checked in successfully"));
    }

    @PostMapping("/check-out")
    @Operation(summary = "Clock out and compute work duration")
    public ResponseEntity<ApiResponse<AttendanceDto>> checkOut(@AuthenticationPrincipal UserPrincipal principal) {
        AttendanceDto dto = attendanceService.checkOut(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(dto, "Checked out successfully"));
    }

    @GetMapping("/my-records")
    @Operation(summary = "Get personal attendance history")
    public ResponseEntity<ApiResponse<Page<AttendanceDto>>> getMyAttendance(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable) {
        Page<AttendanceDto> page = attendanceService.getMyAttendance(principal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(page));
    }

    @GetMapping("/company-records")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get company-wide attendance records with optional date filter")
    public ResponseEntity<ApiResponse<Page<AttendanceDto>>> getCompanyAttendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable) {
        Page<AttendanceDto> page = attendanceService.getCompanyAttendance(date, pageable);
        return ResponseEntity.ok(ApiResponse.ok(page));
    }

    @PostMapping("/{recordId}/correct")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "HR correction of attendance record with mandatory audit rationale")
    public ResponseEntity<ApiResponse<AttendanceDto>> correctAttendance(
            @PathVariable UUID recordId,
            @Valid @RequestBody AttendanceCorrectionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        AttendanceDto dto = attendanceService.correctAttendance(recordId, request, principal.getId(), principal.getEmail());
        return ResponseEntity.ok(ApiResponse.ok(dto, "Attendance record corrected"));
    }
}
