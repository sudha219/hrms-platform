package com.hrms.repository;

import com.hrms.entity.Payslip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayslipRepository extends JpaRepository<Payslip, UUID> {
    Page<Payslip> findByCompanyIdAndEmployeeId(UUID companyId, UUID employeeId, Pageable pageable);
    List<Payslip> findByCompanyIdAndPayrollRunId(UUID companyId, UUID payrollRunId);
    Optional<Payslip> findByCompanyIdAndId(UUID companyId, UUID id);
    Optional<Payslip> findByCompanyIdAndPayrollRunIdAndEmployeeId(UUID companyId, UUID payrollRunId, UUID employeeId);
}
