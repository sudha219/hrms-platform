package com.hrms.controller;

import com.hrms.common.ApiResponse;
import com.hrms.dto.payroll.GeneratePayrollRequest;
import com.hrms.dto.payroll.PayrollRunDto;
import com.hrms.dto.payroll.PayslipDto;
import com.hrms.security.UserPrincipal;
import com.hrms.service.PayrollService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payroll")
@RequiredArgsConstructor
@Tag(name = "Payroll & Payslips", description = "Endpoints for draft payroll runs, authorization, and employee payslips")
public class PayrollController {

    private final PayrollService payrollService;

    @PostMapping("/runs/generate")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Generate a draft payroll run calculation for a specific pay period")
    public ResponseEntity<ApiResponse<PayrollRunDto>> generatePayroll(
            @Valid @RequestBody GeneratePayrollRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        PayrollRunDto dto = payrollService.generateDraftPayroll(request, principal.getId(), principal.getEmail());
        return ResponseEntity.ok(ApiResponse.ok(dto, "Draft payroll generated successfully"));
    }

    @PatchMapping("/runs/{runId}/approve")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Authorize and finalize a draft payroll run")
    public ResponseEntity<ApiResponse<PayrollRunDto>> approvePayroll(
            @PathVariable UUID runId,
            @AuthenticationPrincipal UserPrincipal principal) {
        PayrollRunDto dto = payrollService.approvePayroll(runId, principal.getId(), principal.getEmail());
        return ResponseEntity.ok(ApiResponse.ok(dto, "Payroll run approved successfully"));
    }

    @GetMapping("/payslips/my-payslips")
    @Operation(summary = "Retrieve secure list of employee personal payslips")
    public ResponseEntity<ApiResponse<Page<PayslipDto>>> getMyPayslips(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable) {
        Page<PayslipDto> page = payrollService.getMyPayslips(principal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(page));
    }
}
