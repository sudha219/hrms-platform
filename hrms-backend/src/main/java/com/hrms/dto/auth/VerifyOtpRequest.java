package com.hrms.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOtpRequest {

    @NotBlank(message = "Temporary token is required")
    private String tempToken;

    @NotBlank(message = "OTP code is required")
    private String otp;
}
