package com.hrms.config;

import com.hrms.entity.*;
import com.hrms.enums.CompanyStatus;
import com.hrms.enums.Role;
import com.hrms.enums.UserStatus;
import com.hrms.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final SalaryStructureRepository salaryStructureRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        log.info("Seeding initial HRMS multi-tenant data...");

        // 1. Super Admin User
        User superAdmin = User.builder()
                .email("superadmin@hrms.com")
                .passwordHash(passwordEncoder.encode("Admin@123"))
                .status(UserStatus.ACTIVE)
                .verified(true)
                .build();
        superAdmin = userRepository.save(superAdmin);

        // 2. Sample Company (Acme Corp)
        Company acme = Company.builder()
                .code("ACME")
                .name("Acme Corporation")
                .contactEmail("hr@acme.com")
                .contactPhone("+1 555 0199")
                .timezone("America/New_York")
                .status(CompanyStatus.ACTIVE)
                .build();
        acme = companyRepository.save(acme);

        // Super Admin Membership in Acme
        CompanyMembership superAdminMembership = CompanyMembership.builder()
                .company(acme)
                .user(superAdmin)
                .role(Role.SUPER_ADMIN)
                .build();
        membershipRepository.save(superAdminMembership);

        // 3. Departments in Acme
        Department engDept = Department.builder()
                .name("Engineering")
                .code("ENG")
                .status("ACTIVE")
                .build();
        engDept.setCompanyId(acme.getId());
        engDept = departmentRepository.save(engDept);

        Department hrDept = Department.builder()
                .name("Human Resources")
                .code("HR")
                .status("ACTIVE")
                .build();
        hrDept.setCompanyId(acme.getId());
        hrDept = departmentRepository.save(hrDept);

        // 4. Company Admin (HR Manager)
        User hrUser = User.builder()
                .email("admin@acme.com")
                .passwordHash(passwordEncoder.encode("AcmeAdmin@123"))
                .status(UserStatus.ACTIVE)
                .verified(true)
                .build();
        hrUser = userRepository.save(hrUser);

        membershipRepository.save(CompanyMembership.builder()
                .company(acme)
                .user(hrUser)
                .role(Role.COMPANY_ADMIN)
                .build());

        Employee hrEmployee = Employee.builder()
                .userId(hrUser.getId())
                .department(hrDept)
                .employeeCode("EMP-ACME-001")
                .firstName("Sarah")
                .lastName("Connor")
                .workEmail("admin@acme.com")
                .phone("+1 555 0101")
                .designation("Head of HR")
                .joiningDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .build();
        hrEmployee.setCompanyId(acme.getId());
        hrEmployee = employeeRepository.save(hrEmployee);

        // 5. Engineering Manager
        User managerUser = User.builder()
                .email("manager@acme.com")
                .passwordHash(passwordEncoder.encode("Manager@123"))
                .status(UserStatus.ACTIVE)
                .verified(true)
                .build();
        managerUser = userRepository.save(managerUser);

        membershipRepository.save(CompanyMembership.builder()
                .company(acme)
                .user(managerUser)
                .role(Role.MANAGER)
                .build());

        Employee managerEmployee = Employee.builder()
                .userId(managerUser.getId())
                .department(engDept)
                .employeeCode("EMP-ACME-002")
                .firstName("John")
                .lastName("Doe")
                .workEmail("manager@acme.com")
                .phone("+1 555 0102")
                .designation("Engineering Manager")
                .joiningDate(LocalDate.of(2023, 3, 1))
                .status("ACTIVE")
                .build();
        managerEmployee.setCompanyId(acme.getId());
        managerEmployee = employeeRepository.save(managerEmployee);

        // 6. Regular Employee (Reports to John Doe)
        User empUser = User.builder()
                .email("employee@acme.com")
                .passwordHash(passwordEncoder.encode("Employee@123"))
                .status(UserStatus.ACTIVE)
                .verified(true)
                .build();
        empUser = userRepository.save(empUser);

        membershipRepository.save(CompanyMembership.builder()
                .company(acme)
                .user(empUser)
                .role(Role.EMPLOYEE)
                .build());

        Employee regularEmployee = Employee.builder()
                .userId(empUser.getId())
                .department(engDept)
                .manager(managerEmployee)
                .employeeCode("EMP-ACME-003")
                .firstName("Alice")
                .lastName("Smith")
                .workEmail("employee@acme.com")
                .phone("+1 555 0103")
                .designation("Software Engineer")
                .joiningDate(LocalDate.of(2024, 1, 10))
                .status("ACTIVE")
                .build();
        regularEmployee.setCompanyId(acme.getId());
        regularEmployee = employeeRepository.save(regularEmployee);

        // 7. Leave Types in Acme
        LeaveType paidLeave = LeaveType.builder()
                .name("Paid Annual Leave")
                .daysPerYear(20)
                .carryForwardMax(5)
                .requiresApproval(true)
                .build();
        paidLeave.setCompanyId(acme.getId());
        paidLeave = leaveTypeRepository.save(paidLeave);

        LeaveType sickLeave = LeaveType.builder()
                .name("Sick Leave")
                .daysPerYear(10)
                .carryForwardMax(0)
                .requiresApproval(true)
                .build();
        sickLeave.setCompanyId(acme.getId());
        sickLeave = leaveTypeRepository.save(sickLeave);

        // 8. Leave Balances for Alice Smith
        LeaveBalance alicePaidBalance = LeaveBalance.builder()
                .employee(regularEmployee)
                .leaveType(paidLeave)
                .year(LocalDate.now().getYear())
                .allocatedDays(new BigDecimal("20.00"))
                .usedDays(new BigDecimal("2.00"))
                .build();
        alicePaidBalance.setCompanyId(acme.getId());
        leaveBalanceRepository.save(alicePaidBalance);

        // 9. Salary Structure for Alice Smith
        SalaryStructure aliceSalary = SalaryStructure.builder()
                .employee(regularEmployee)
                .basicSalary(new BigDecimal("5000.00"))
                .hraAllowance(new BigDecimal("1500.00"))
                .specialAllowance(new BigDecimal("800.00"))
                .taxDeduction(new BigDecimal("600.00"))
                .providentFund(new BigDecimal("400.00"))
                .effectiveFrom(LocalDate.of(2024, 1, 10))
                .build();
        aliceSalary.setCompanyId(acme.getId());
        salaryStructureRepository.save(aliceSalary);

        log.info("Default seed data initialized successfully!");
        log.info("Credentials summary:");
        log.info("  Super Admin:   superadmin@hrms.com / Admin@123");
        log.info("  Company Admin: admin@acme.com / AcmeAdmin@123 (Tenant: ACME)");
        log.info("  Manager:       manager@acme.com / Manager@123 (Tenant: ACME)");
        log.info("  Employee:      employee@acme.com / Employee@123 (Tenant: ACME)");
    }
}
