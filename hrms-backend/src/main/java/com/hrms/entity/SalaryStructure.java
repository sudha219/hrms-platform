package com.hrms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "salary_structures")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryStructure extends BaseTenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "basic_salary", precision = 12, scale = 2, nullable = false)
    private BigDecimal basicSalary;

    @Column(name = "hra_allowance", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal hraAllowance = BigDecimal.ZERO;

    @Column(name = "special_allowance", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal specialAllowance = BigDecimal.ZERO;

    @Column(name = "tax_deduction", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal taxDeduction = BigDecimal.ZERO;

    @Column(name = "provident_fund", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal providentFund = BigDecimal.ZERO;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;
}
