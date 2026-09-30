package com.hrms.service;

import com.hrms.dto.platform.CreateCompanyRequest;
import com.hrms.entity.Company;
import com.hrms.entity.CompanyMembership;
import com.hrms.entity.Employee;
import com.hrms.entity.User;
import com.hrms.enums.CompanyStatus;
import com.hrms.enums.Role;
import com.hrms.enums.UserStatus;
import com.hrms.exception.ApiException;
import com.hrms.repository.CompanyMembershipRepository;
import com.hrms.repository.CompanyRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    @Transactional
    public Company createCompany(CreateCompanyRequest request, UUID actorId, String actorEmail) {
        if (companyRepository.existsByCode(request.getCode().toUpperCase().trim())) {
            throw new ApiException("Company code already exists: " + request.getCode());
        }

        if (userRepository.existsByEmail(request.getAdminEmail().toLowerCase().trim())) {
            throw new ApiException("Admin email already registered: " + request.getAdminEmail());
        }

        // 1. Create Company
        Company company = Company.builder()
                .code(request.getCode().toUpperCase().trim())
                .name(request.getName().trim())
                .contactEmail(request.getContactEmail().trim())
                .contactPhone(request.getContactPhone())
                .timezone(request.getTimezone() != null ? request.getTimezone() : "UTC")
                .status(CompanyStatus.ACTIVE)
                .build();
        company = companyRepository.save(company);

        // 2. Create Company Admin User
        User adminUser = User.builder()
                .email(request.getAdminEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getAdminPassword()))
                .status(UserStatus.ACTIVE)
                .verified(true)
                .build();
        adminUser = userRepository.save(adminUser);

        // 3. Assign Membership
        CompanyMembership membership = CompanyMembership.builder()
                .company(company)
                .user(adminUser)
                .role(Role.COMPANY_ADMIN)
                .build();
        membershipRepository.save(membership);

        // 4. Create Initial Employee Record for Company Admin
        Employee adminEmployee = Employee.builder()
                .userId(adminUser.getId())
                .employeeCode("EMP-ADMIN-001")
                .firstName(request.getAdminFirstName().trim())
                .lastName(request.getAdminLastName().trim())
                .workEmail(request.getAdminEmail().toLowerCase().trim())
                .designation("Company Administrator")
                .joiningDate(LocalDate.now())
                .status("ACTIVE")
                .build();
        adminEmployee.setCompanyId(company.getId());
        employeeRepository.save(adminEmployee);

        // 5. Audit Log
        auditService.logAction(actorId, actorEmail, "CREATE_COMPANY", "Company", company.getId(),
                "Created company " + company.getName() + " and assigned admin " + adminUser.getEmail(), null);

        return company;
    }

    @Transactional
    public Company updateCompanyStatus(UUID companyId, CompanyStatus status, UUID actorId, String actorEmail) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ApiException("Company not found with ID: " + companyId));

        company.setStatus(status);
        company = companyRepository.save(company);

        auditService.logAction(actorId, actorEmail, "UPDATE_COMPANY_STATUS", "Company", company.getId(),
                "Updated status to " + status.name(), null);

        return company;
    }
}
