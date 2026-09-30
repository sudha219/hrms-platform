package com.hrms.controller;

import com.hrms.common.ApiResponse;
import com.hrms.dto.employee.CreateEmployeeRequest;
import com.hrms.dto.employee.EmployeeDto;
import com.hrms.security.UserPrincipal;
import com.hrms.service.EmployeeService;
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
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Directory & Self Service", description = "Employee profile management and team directory")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @Operation(summary = "Get paginated, searchable list of company employees")
    public ResponseEntity<ApiResponse<Page<EmployeeDto>>> getEmployees(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        Page<EmployeeDto> page = employeeService.getEmployees(search, pageable);
        return ResponseEntity.ok(ApiResponse.ok(page));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user's employee profile")
    public ResponseEntity<ApiResponse<EmployeeDto>> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        EmployeeDto dto = employeeService.getEmployeeByUserId(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee profile by ID")
    public ResponseEntity<ApiResponse<EmployeeDto>> getEmployeeById(@PathVariable UUID id) {
        EmployeeDto dto = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Create an employee profile and optional user login account")
    public ResponseEntity<ApiResponse<EmployeeDto>> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        EmployeeDto dto = employeeService.createEmployee(request, principal.getId(), principal.getEmail());
        return ResponseEntity.ok(ApiResponse.ok(dto, "Employee created successfully"));
    }
}
