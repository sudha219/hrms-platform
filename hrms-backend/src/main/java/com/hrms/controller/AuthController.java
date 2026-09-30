package com.hrms.controller;

import com.hrms.common.ApiResponse;
import com.hrms.dto.auth.AuthResponse;
import com.hrms.dto.auth.LoginRequest;
import com.hrms.dto.auth.VerifyOtpRequest;
import com.hrms.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication & OTP", description = "Endpoints for login and 2FA OTP verification")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Sign in with email and password (triggers OTP)")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "OTP sent to your registered email address."));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP and acquire JWT Access Token")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Authentication successful."));
    }
}
