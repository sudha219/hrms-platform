package com.hrms.service;

import com.hrms.common.TenantContextHolder;
import com.hrms.dto.employee.CreateEmployeeRequest;
import com.hrms.dto.employee.EmployeeDto;
import com.hrms.entity.*;
import com.hrms.enums.UserStatus;
import com.hrms.exception.ApiException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public Page<EmployeeDto> getEmployees(String search, Pageable pageable) {
        UUID companyId = TenantContextHolder.getTenantId();
        Page<Employee> page = (search != null && !search.isBlank())
                ? employeeRepository.searchEmployees(companyId, search, pageable)
                : employeeRepository.findByCompanyId(companyId, pageable);

        return page.map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeById(UUID id) {
        UUID companyId = TenantContextHolder.getTenantId();
        Employee employee = employeeRepository.findByCompanyIdAndId(companyId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        return mapToDto(employee);
    }

    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeByUserId(UUID userId) {
        UUID companyId = TenantContextHolder.getTenantId();
        Employee employee = employeeRepository.findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found for user"));
        return mapToDto(employee);
    }

    @Transactional
    public EmployeeDto createEmployee(CreateEmployeeRequest request, UUID actorId, String actorEmail) {
        UUID companyId = TenantContextHolder.getTenantId();

        if (employeeRepository.existsByCompanyIdAndEmployeeCode(companyId, request.getEmployeeCode().trim())) {
            throw new ApiException("Employee code already exists in your company: " + request.getEmployeeCode());
        }

        Department dept = null;
        if (request.getDepartmentId() != null) {
            dept = departmentRepository.findByCompanyIdAndId(companyId, request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        }

        Employee manager = null;
        if (request.getManagerId() != null) {
            manager = employeeRepository.findByCompanyIdAndId(companyId, request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
        }

        UUID userId = null;
        if (request.isCreateLoginAccount()) {
            if (userRepository.existsByEmail(request.getWorkEmail().toLowerCase().trim())) {
                throw new ApiException("User email already registered: " + request.getWorkEmail());
            }

            String password = request.getInitialPassword() != null ? request.getInitialPassword() : "Welcome@123";
            User newUser = User.builder()
                    .email(request.getWorkEmail().toLowerCase().trim())
                    .passwordHash(passwordEncoder.encode(password))
                    .status(UserStatus.ACTIVE)
                    .verified(true)
                    .build();
            newUser = userRepository.save(newUser);
            userId = newUser.getId();

            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

            CompanyMembership membership = CompanyMembership.builder()
                    .company(company)
                    .user(newUser)
                    .role(request.getRole())
                    .build();
            membershipRepository.save(membership);
        }

        Employee employee = Employee.builder()
                .userId(userId)
                .department(dept)
                .manager(manager)
                .employeeCode(request.getEmployeeCode().trim())
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .workEmail(request.getWorkEmail().toLowerCase().trim())
                .phone(request.getPhone())
                .designation(request.getDesignation())
                .joiningDate(request.getJoiningDate())
                .status("ACTIVE")
                .build();
        employee.setCompanyId(companyId);
        employee = employeeRepository.save(employee);

        auditService.logAction(actorId, actorEmail, "CREATE_EMPLOYEE", "Employee", employee.getId(),
                "Created employee " + employee.getFirstName() + " " + employee.getLastName(), null);

        return mapToDto(employee);
    }

    private EmployeeDto mapToDto(Employee emp) {
        return EmployeeDto.builder()
                .id(emp.getId())
                .companyId(emp.getCompanyId())
                .userId(emp.getUserId())
                .employeeCode(emp.getEmployeeCode())
                .firstName(emp.getFirstName())
                .lastName(emp.getLastName())
                .workEmail(emp.getWorkEmail())
                .phone(emp.getPhone())
                .designation(emp.getDesignation())
                .departmentId(emp.getDepartment() != null ? emp.getDepartment().getId() : null)
                .departmentName(emp.getDepartment() != null ? emp.getDepartment().getName() : null)
                .managerId(emp.getManager() != null ? emp.getManager().getId() : null)
                .managerName(emp.getManager() != null ? emp.getManager().getFirstName() + " " + emp.getManager().getLastName() : null)
                .joiningDate(emp.getJoiningDate())
                .status(emp.getStatus())
                .build();
    }
}
