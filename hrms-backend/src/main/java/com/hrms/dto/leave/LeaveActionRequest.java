package com.hrms.dto.leave;

import com.hrms.enums.LeaveStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeaveActionRequest {

    @NotNull(message = "Action status is required (APPROVED or REJECTED)")
    private LeaveStatus status;

    private String comment;
}
