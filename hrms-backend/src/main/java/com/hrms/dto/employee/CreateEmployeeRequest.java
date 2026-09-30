package com.hrms.dto.employee;

import com.hrms.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateEmployeeRequest {

    @NotBlank(message = "Employee code is required")
    private String employeeCode;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Work email is required")
    @Email(message = "Invalid work email")
    private String workEmail;

    private String phone;
    private String designation;
    private UUID departmentId;
    private UUID managerId;

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    // Optional user account provisioning
    private boolean createLoginAccount = true;
    private Role role = Role.EMPLOYEE;
    private String initialPassword;
}
