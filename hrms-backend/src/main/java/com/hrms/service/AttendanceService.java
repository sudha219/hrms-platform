package com.hrms.service;

import com.hrms.common.TenantContextHolder;
import com.hrms.dto.attendance.AttendanceCorrectionRequest;
import com.hrms.dto.attendance.AttendanceDto;
import com.hrms.dto.attendance.CheckInRequest;
import com.hrms.entity.AttendanceRecord;
import com.hrms.entity.Employee;
import com.hrms.enums.AttendanceStatus;
import com.hrms.enums.SourceChannel;
import com.hrms.exception.ApiException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.AttendanceRecordRepository;
import com.hrms.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    @Transactional
    public AttendanceDto checkIn(UUID userId, CheckInRequest request) {
        UUID companyId = TenantContextHolder.getTenantId();
        Employee employee = employeeRepository.findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found for user"));

        LocalDate today = LocalDate.now();

        Optional<AttendanceRecord> existing = attendanceRepository
                .findTopByCompanyIdAndEmployeeIdAndDateOrderByCheckInDesc(companyId, employee.getId(), today);

        if (existing.isPresent() && existing.get().getCheckOut() == null) {
            throw new ApiException("You are already checked in. Please check out first.");
        }

        AttendanceRecord record = AttendanceRecord.builder()
                .employee(employee)
                .date(today)
                .checkIn(Instant.now())
                .status(AttendanceStatus.PRESENT)
                .sourceChannel(request.getSourceChannel() != null ? request.getSourceChannel() : SourceChannel.WEB)
                .totalMinutes(0)
                .build();
        record.setCompanyId(companyId);
        record = attendanceRepository.save(record);

        return mapToDto(record);
    }

    @Transactional
    public AttendanceDto checkOut(UUID userId) {
        UUID companyId = TenantContextHolder.getTenantId();
        Employee employee = employeeRepository.findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found for user"));

        LocalDate today = LocalDate.now();

        AttendanceRecord record = attendanceRepository
                .findTopByCompanyIdAndEmployeeIdAndDateOrderByCheckInDesc(companyId, employee.getId(), today)
                .orElseThrow(() -> new ApiException("No active check-in record found for today."));

        if (record.getCheckOut() != null) {
            throw new ApiException("You have already checked out.");
        }

        Instant checkOutTime = Instant.now();
        record.setCheckOut(checkOutTime);

        long minutes = Duration.between(record.getCheckIn(), checkOutTime).toMinutes();
        record.setTotalMinutes((int) minutes);

        if (minutes < 240) { // Less than 4 hours
            record.setStatus(AttendanceStatus.HALF_DAY);
        }

        record = attendanceRepository.save(record);
        return mapToDto(record);
    }

    @Transactional(readOnly = true)
    public Page<AttendanceDto> getMyAttendance(UUID userId, Pageable pageable) {
        UUID companyId = TenantContextHolder.getTenantId();
        Employee employee = employeeRepository.findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found for user"));

        return attendanceRepository.findByCompanyIdAndEmployeeId(companyId, employee.getId(), pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<AttendanceDto> getCompanyAttendance(LocalDate date, Pageable pageable) {
        UUID companyId = TenantContextHolder.getTenantId();
        if (date != null) {
            return attendanceRepository.findByCompanyIdAndDate(companyId, date, pageable).map(this::mapToDto);
        }
        return attendanceRepository.findByCompanyId(companyId, pageable).map(this::mapToDto);
    }

    @Transactional
    public AttendanceDto correctAttendance(UUID recordId, AttendanceCorrectionRequest request, UUID actorId, String actorEmail) {
        UUID companyId = TenantContextHolder.getTenantId();
        AttendanceRecord record = attendanceRepository.findById(recordId)
                .filter(r -> r.getCompanyId().equals(companyId))
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found"));

        record.setCheckIn(request.getCheckIn());
        if (request.getCheckOut() != null) {
            record.setCheckOut(request.getCheckOut());
            long minutes = Duration.between(request.getCheckIn(), request.getCheckOut()).toMinutes();
            record.setTotalMinutes((int) minutes);
        }
        if (request.getStatus() != null) {
            record.setStatus(request.getStatus());
        }
        record.setCorrectionReason(request.getReason());
        record.setCorrectedBy(actorId);

        record = attendanceRepository.save(record);

        auditService.logAction(actorId, actorEmail, "CORRECT_ATTENDANCE", "AttendanceRecord", record.getId(),
                "Corrected attendance. Reason: " + request.getReason(), null);

        return mapToDto(record);
    }

    private AttendanceDto mapToDto(AttendanceRecord record) {
        return AttendanceDto.builder()
                .id(record.getId())
                .employeeId(record.getEmployee().getId())
                .employeeName(record.getEmployee().getFirstName() + " " + record.getEmployee().getLastName())
                .employeeCode(record.getEmployee().getEmployeeCode())
                .date(record.getDate())
                .checkIn(record.getCheckIn())
                .checkOut(record.getCheckOut())
                .totalMinutes(record.getTotalMinutes())
                .status(record.getStatus())
                .sourceChannel(record.getSourceChannel())
                .correctionReason(record.getCorrectionReason())
                .build();
    }
}
