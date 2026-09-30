package com.hrms.controller;

import com.hrms.common.ApiResponse;
import com.hrms.dto.leave.ApplyLeaveRequest;
import com.hrms.dto.leave.LeaveActionRequest;
import com.hrms.dto.leave.LeaveRequestDto;
import com.hrms.entity.LeaveBalance;
import com.hrms.security.UserPrincipal;
import com.hrms.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
@Tag(name = "Leave Management", description = "Endpoints for applying for leave, checking balance, and approvals")
public class LeaveController {

    private final LeaveService leaveService;

    @GetMapping("/balances")
    @Operation(summary = "Get current leave balances for the authenticated employee")
    public ResponseEntity<ApiResponse<List<LeaveBalance>>> getMyBalances(@AuthenticationPrincipal UserPrincipal principal) {
        List<LeaveBalance> balances = leaveService.getMyBalances(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(balances));
    }

    @PostMapping("/requests")
    @Operation(summary = "Submit a new leave request (checks balance & overlapping)")
    public ResponseEntity<ApiResponse<LeaveRequestDto>> applyLeave(
            @Valid @RequestBody ApplyLeaveRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        LeaveRequestDto dto = leaveService.applyLeave(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok(dto, "Leave request submitted"));
    }

    @GetMapping("/my-requests")
    @Operation(summary = "Get paginated history of personal leave requests")
    public ResponseEntity<ApiResponse<Page<LeaveRequestDto>>> getMyLeaveRequests(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable) {
        Page<LeaveRequestDto> page = leaveService.getMyLeaveRequests(principal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(page));
    }

    @GetMapping("/team-requests")
    @PreAuthorize("hasAnyRole('MANAGER', 'COMPANY_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get pending leave requests for reporting team members")
    public ResponseEntity<ApiResponse<List<LeaveRequestDto>>> getTeamPendingRequests(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<LeaveRequestDto> list = leaveService.getTeamPendingRequests(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PatchMapping("/requests/{requestId}/action")
    @PreAuthorize("hasAnyRole('MANAGER', 'COMPANY_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Approve or Reject a leave request")
    public ResponseEntity<ApiResponse<LeaveRequestDto>> processLeaveAction(
            @PathVariable UUID requestId,
            @Valid @RequestBody LeaveActionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        LeaveRequestDto dto = leaveService.processLeaveAction(requestId, request, principal.getId(), principal.getEmail());
        return ResponseEntity.ok(ApiResponse.ok(dto, "Leave request updated to " + request.getStatus()));
    }
}
