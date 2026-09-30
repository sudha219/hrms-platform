package com.hrms.dto.payroll;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayslipDto {
    private UUID id;
    private UUID payrollRunId;
    private UUID employeeId;
    private String employeeName;
    private String employeeCode;
    private BigDecimal grossPay;
    private BigDecimal totalDeductions;
    private BigDecimal netPay;
    private String fileStorageKey;
}
