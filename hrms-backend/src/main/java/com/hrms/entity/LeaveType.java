package com.hrms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "leave_types")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveType extends BaseTenantEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "days_per_year", nullable = false)
    private Integer daysPerYear;

    @Column(name = "carry_forward_max")
    @Builder.Default
    private Integer carryForwardMax = 0;

    @Column(name = "requires_approval")
    @Builder.Default
    private Boolean requiresApproval = true;
}
