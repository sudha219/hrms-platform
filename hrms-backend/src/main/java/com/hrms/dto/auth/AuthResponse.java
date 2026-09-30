package com.hrms.dto.auth;

import com.hrms.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private boolean otpRequired;
    private String tempToken;
    private String accessToken;
    private String tokenType;
    private UUID userId;
    private String email;
    private UUID companyId;
    private String companyName;
    private Role role;
}
