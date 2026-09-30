package com.hrms.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {
    private UUID id;
    private UUID companyId;
    private UUID userId;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String workEmail;
    private String phone;
    private String designation;
    private UUID departmentId;
    private String departmentName;
    private UUID managerId;
    private String managerName;
    private LocalDate joiningDate;
    private String status;
}
