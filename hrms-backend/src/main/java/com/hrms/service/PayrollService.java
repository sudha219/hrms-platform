package com.hrms.service;

import com.hrms.common.TenantContextHolder;
import com.hrms.dto.payroll.GeneratePayrollRequest;
import com.hrms.dto.payroll.PayrollRunDto;
import com.hrms.dto.payroll.PayslipDto;
import com.hrms.entity.*;
import com.hrms.enums.PayrollStatus;
import com.hrms.exception.ApiException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final PayrollRunRepository payrollRunRepository;
    private final PayslipRepository payslipRepository;
    private final SalaryStructureRepository salaryStructureRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    @Transactional
    public PayrollRunDto generateDraftPayroll(GeneratePayrollRequest request, UUID actorUserId, String actorEmail) {
        UUID companyId = TenantContextHolder.getTenantId();

        if (payrollRunRepository.findByCompanyIdAndPeriodStartAndPeriodEnd(companyId, request.getPeriodStart(), request.getPeriodEnd()).isPresent()) {
            throw new ApiException("A payroll run already exists for this exact period.");
        }

        List<Employee> activeEmployees = employeeRepository.findByCompanyId(companyId);

        PayrollRun payrollRun = PayrollRun.builder()
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .status(PayrollStatus.DRAFT)
                .createdBy(actorUserId)
                .build();
        payrollRun.setCompanyId(companyId);
        payrollRun = payrollRunRepository.save(payrollRun);

        BigDecimal grandGross = BigDecimal.ZERO;
        BigDecimal grandDeductions = BigDecimal.ZERO;
        BigDecimal grandNet = BigDecimal.ZERO;

        for (Employee emp : activeEmployees) {
            SalaryStructure structure = salaryStructureRepository
                    .findTopByCompanyIdAndEmployeeIdOrderByEffectiveFromDesc(companyId, emp.getId())
                    .orElseGet(() -> SalaryStructure.builder()
                            .basicSalary(new BigDecimal("3000.00"))
                            .hraAllowance(new BigDecimal("1000.00"))
                            .specialAllowance(new BigDecimal("500.00"))
                            .taxDeduction(new BigDecimal("300.00"))
                            .providentFund(new BigDecimal("200.00"))
                            .build());

            BigDecimal gross = structure.getBasicSalary()
                    .add(structure.getHraAllowance())
                    .add(structure.getSpecialAllowance());

            BigDecimal deductions = structure.getTaxDeduction()
                    .add(structure.getProvidentFund());

            BigDecimal net = gross.subtract(deductions);

            grandGross = grandGross.add(gross);
            grandDeductions = grandDeductions.add(deductions);
            grandNet = grandNet.add(net);

            Payslip payslip = Payslip.builder()
                    .payrollRun(payrollRun)
                    .employee(emp)
                    .grossPay(gross)
                    .totalDeductions(deductions)
                    .netPay(net)
                    .build();
            payslip.setCompanyId(companyId);
            payslipRepository.save(payslip);
        }

        payrollRun.setTotalGross(grandGross);
        payrollRun.setTotalDeductions(grandDeductions);
        payrollRun.setTotalNet(grandNet);
        payrollRun = payrollRunRepository.save(payrollRun);

        auditService.logAction(actorUserId, actorEmail, "GENERATE_DRAFT_PAYROLL", "PayrollRun", payrollRun.getId(),
                "Generated draft payroll for period " + request.getPeriodStart() + " to " + request.getPeriodEnd(), null);

        return mapToDto(payrollRun);
    }

    @Transactional
    public PayrollRunDto approvePayroll(UUID payrollRunId, UUID actorUserId, String actorEmail) {
        UUID companyId = TenantContextHolder.getTenantId();
        PayrollRun run = payrollRunRepository.findByCompanyIdAndId(companyId, payrollRunId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll run not found"));

        if (run.getStatus() != PayrollStatus.DRAFT) {
            throw new ApiException("Only DRAFT payroll runs can be approved.");
        }

        run.setStatus(PayrollStatus.APPROVED);
        run.setApprovedBy(actorUserId);
        run = payrollRunRepository.save(run);

        auditService.logAction(actorUserId, actorEmail, "APPROVE_PAYROLL", "PayrollRun", run.getId(),
                "Approved payroll run for period " + run.getPeriodStart() + " to " + run.getPeriodEnd(), null);

        return mapToDto(run);
    }

    @Transactional(readOnly = true)
    public Page<PayslipDto> getMyPayslips(UUID userId, Pageable pageable) {
        UUID companyId = TenantContextHolder.getTenantId();
        Employee employee = employeeRepository.findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found"));

        return payslipRepository.findByCompanyIdAndEmployeeId(companyId, employee.getId(), pageable)
                .map(this::mapPayslipToDto);
    }

    private PayrollRunDto mapToDto(PayrollRun run) {
        return PayrollRunDto.builder()
                .id(run.getId())
                .periodStart(run.getPeriodStart())
                .periodEnd(run.getPeriodEnd())
                .totalGross(run.getTotalGross())
                .totalDeductions(run.getTotalDeductions())
                .totalNet(run.getTotalNet())
                .status(run.getStatus())
                .build();
    }

    private PayslipDto mapPayslipToDto(Payslip p) {
        return PayslipDto.builder()
                .id(p.getId())
                .payrollRunId(p.getPayrollRun().getId())
                .employeeId(p.getEmployee().getId())
                .employeeName(p.getEmployee().getFirstName() + " " + p.getEmployee().getLastName())
                .employeeCode(p.getEmployee().getEmployeeCode())
                .grossPay(p.getGrossPay())
                .totalDeductions(p.getTotalDeductions())
                .netPay(p.getNetPay())
                .fileStorageKey(p.getFileStorageKey())
                .build();
    }
}
