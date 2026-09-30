package com.hrms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "leave_balances", uniqueConstraints = {
    @UniqueConstraint(name = "uq_emp_leave_year", columnNames = {"employee_id", "leave_type_id", "year"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalance extends BaseTenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "allocated_days", precision = 5, scale = 2, nullable = false)
    private BigDecimal allocatedDays;

    @Column(name = "used_days", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal usedDays = BigDecimal.ZERO;
}
