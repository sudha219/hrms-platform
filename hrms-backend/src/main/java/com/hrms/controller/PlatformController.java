package com.hrms.controller;

import com.hrms.common.ApiResponse;
import com.hrms.dto.platform.CreateCompanyRequest;
import com.hrms.entity.Company;
import com.hrms.enums.CompanyStatus;
import com.hrms.security.UserPrincipal;
import com.hrms.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/platform")
@RequiredArgsConstructor
@Tag(name = "Platform Super Admin", description = "Tenant provisioning and company lifecycle management")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class PlatformController {

    private final CompanyService companyService;

    @GetMapping("/companies")
    @Operation(summary = "List all company tenants across the platform")
    public ResponseEntity<ApiResponse<List<Company>>> listCompanies() {
        List<Company> companies = companyService.getAllCompanies();
        return ResponseEntity.ok(ApiResponse.ok(companies));
    }

    @PostMapping("/companies")
    @Operation(summary = "Provision a new tenant company with initial Company Admin")
    public ResponseEntity<ApiResponse<Company>> createCompany(
            @Valid @RequestBody CreateCompanyRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Company company = companyService.createCompany(request, principal.getId(), principal.getEmail());
        return ResponseEntity.ok(ApiResponse.ok(company, "Company tenant provisioned successfully"));
    }

    @PatchMapping("/companies/{id}/status")
    @Operation(summary = "Update tenant status (ACTIVE, SUSPENDED, ARCHIVED)")
    public ResponseEntity<ApiResponse<Company>> updateCompanyStatus(
            @PathVariable UUID id,
            @RequestParam CompanyStatus status,
            @AuthenticationPrincipal UserPrincipal principal) {
        Company company = companyService.updateCompanyStatus(id, status, principal.getId(), principal.getEmail());
        return ResponseEntity.ok(ApiResponse.ok(company, "Company status updated to " + status));
    }
}
