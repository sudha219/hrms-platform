package com.hrms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "payslips")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payslip extends BaseTenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_run_id", nullable = false)
    private PayrollRun payrollRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "gross_pay", precision = 12, scale = 2, nullable = false)
    private BigDecimal grossPay;

    @Column(name = "total_deductions", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalDeductions;

    @Column(name = "net_pay", precision = 12, scale = 2, nullable = false)
    private BigDecimal netPay;

    @Column(name = "file_storage_key", length = 500)
    private String fileStorageKey;
}
