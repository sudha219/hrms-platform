package com.hrms.service;

import com.hrms.common.TenantContextHolder;
import com.hrms.dto.leave.ApplyLeaveRequest;
import com.hrms.dto.leave.LeaveActionRequest;
import com.hrms.dto.leave.LeaveRequestDto;
import com.hrms.entity.*;
import com.hrms.enums.LeaveStatus;
import com.hrms.exception.ApiException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<LeaveBalance> getMyBalances(UUID userId) {
        UUID companyId = TenantContextHolder.getTenantId();
        Employee employee = employeeRepository.findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found"));

        int currentYear = LocalDate.now().getYear();
        return leaveBalanceRepository.findByCompanyIdAndEmployeeIdAndYear(companyId, employee.getId(), currentYear);
    }

    @Transactional
    public LeaveRequestDto applyLeave(UUID userId, ApplyLeaveRequest request) {
        UUID companyId = TenantContextHolder.getTenantId();
        Employee employee = employeeRepository.findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found"));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ApiException("End date cannot be before start date.");
        }

        // Check for overlapping leaves
        if (leaveRequestRepository.hasOverlappingLeave(companyId, employee.getId(), request.getStartDate(), request.getEndDate())) {
            throw new ApiException("You already have an existing leave request during this date range.");
        }

        LeaveType leaveType = leaveTypeRepository.findById(request.getLeaveTypeId())
                .filter(lt -> lt.getCompanyId().equals(companyId))
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found"));

        // Check and validate leave balance
        int currentYear = request.getStartDate().getYear();
        LeaveBalance balance = leaveBalanceRepository
                .findByCompanyIdAndEmployeeIdAndLeaveTypeIdAndYear(companyId, employee.getId(), leaveType.getId(), currentYear)
                .orElseGet(() -> {
                    // Create default balance if not existing
                    LeaveBalance nb = LeaveBalance.builder()
                            .employee(employee)
                            .leaveType(leaveType)
                            .year(currentYear)
                            .allocatedDays(BigDecimal.valueOf(leaveType.getDaysPerYear()))
                            .usedDays(BigDecimal.ZERO)
                            .build();
                    nb.setCompanyId(companyId);
                    return leaveBalanceRepository.save(nb);
                });

        BigDecimal available = balance.getAllocatedDays().subtract(balance.getUsedDays());
        if (request.getTotalDays().compareTo(available) > 0) {
            throw new ApiException("Insufficient leave balance. Available: " + available + " days, requested: " + request.getTotalDays());
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(leaveType)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalDays(request.getTotalDays())
                .reason(request.getReason())
                .status(LeaveStatus.PENDING)
                .approver(employee.getManager())
                .build();
        leaveRequest.setCompanyId(companyId);
        leaveRequest = leaveRequestRepository.save(leaveRequest);

        return mapToDto(leaveRequest);
    }

    @Transactional(readOnly = true)
    public Page<LeaveRequestDto> getMyLeaveRequests(UUID userId, Pageable pageable) {
        UUID companyId = TenantContextHolder.getTenantId();
        Employee employee = employeeRepository.findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found"));

        return leaveRequestRepository.findByCompanyIdAndEmployeeId(companyId, employee.getId(), pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestDto> getTeamPendingRequests(UUID managerUserId) {
        UUID companyId = TenantContextHolder.getTenantId();
        Employee manager = employeeRepository.findByCompanyIdAndUserId(companyId, managerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager profile not found"));

        return leaveRequestRepository.findPendingByManager(companyId, manager.getId(), LeaveStatus.PENDING)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional
    public LeaveRequestDto processLeaveAction(UUID requestId, LeaveActionRequest request, UUID actorUserId, String actorEmail) {
        UUID companyId = TenantContextHolder.getTenantId();
        LeaveRequest leaveRequest = leaveRequestRepository.findByCompanyIdAndId(companyId, requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new ApiException("Leave request is already " + leaveRequest.getStatus());
        }

        Employee approver = employeeRepository.findByCompanyIdAndUserId(companyId, actorUserId)
                .orElse(null);

        leaveRequest.setStatus(request.getStatus());
        leaveRequest.setApprover(approver);
        leaveRequest.setApproverComment(request.getComment());

        if (request.getStatus() == LeaveStatus.APPROVED) {
            // Deduct balance
            int year = leaveRequest.getStartDate().getYear();
            LeaveBalance balance = leaveBalanceRepository
                    .findByCompanyIdAndEmployeeIdAndLeaveTypeIdAndYear(
                            companyId, leaveRequest.getEmployee().getId(), leaveRequest.getLeaveType().getId(), year
                    ).orElseThrow(() -> new ApiException("Leave balance record missing"));

            balance.setUsedDays(balance.getUsedDays().add(leaveRequest.getTotalDays()));
            leaveBalanceRepository.save(balance);
        }

        leaveRequest = leaveRequestRepository.save(leaveRequest);

        auditService.logAction(actorUserId, actorEmail, "PROCESS_LEAVE_" + request.getStatus(), "LeaveRequest",
                leaveRequest.getId(), "Leave request set to " + request.getStatus(), null);

        return mapToDto(leaveRequest);
    }

    private LeaveRequestDto mapToDto(LeaveRequest req) {
        return LeaveRequestDto.builder()
                .id(req.getId())
                .employeeId(req.getEmployee().getId())
                .employeeName(req.getEmployee().getFirstName() + " " + req.getEmployee().getLastName())
                .leaveTypeId(req.getLeaveType().getId())
                .leaveTypeName(req.getLeaveType().getName())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .totalDays(req.getTotalDays())
                .reason(req.getReason())
                .status(req.getStatus())
                .approverName(req.getApprover() != null ? req.getApprover().getFirstName() + " " + req.getApprover().getLastName() : null)
                .approverComment(req.getApproverComment())
                .build();
    }
}
