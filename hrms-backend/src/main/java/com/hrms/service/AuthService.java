package com.hrms.service;

import com.hrms.dto.auth.AuthResponse;
import com.hrms.dto.auth.LoginRequest;
import com.hrms.dto.auth.VerifyOtpRequest;
import com.hrms.entity.CompanyMembership;
import com.hrms.entity.User;
import com.hrms.enums.Role;
import com.hrms.enums.UserStatus;
import com.hrms.exception.ApiException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.CompanyMembershipRepository;
import com.hrms.repository.UserRepository;
import com.hrms.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final OtpService otpService;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new ApiException("Invalid email or password"));

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new ApiException("Account is suspended. Please contact your administrator.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ApiException("Invalid email or password");
        }

        // Fetch company membership
        List<CompanyMembership> memberships = membershipRepository.findByUserId(user.getId());
        UUID companyId = memberships.isEmpty() ? null : memberships.get(0).getCompany().getId();

        // Trigger OTP verification
        otpService.generateAndSendOtp(user.getEmail());

        // Generate temporary token for OTP flow
        String tempToken = tokenProvider.generateOtpTemporaryToken(user.getId(), user.getEmail(), companyId);

        return AuthResponse.builder()
                .otpRequired(true)
                .tempToken(tempToken)
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        if (!tokenProvider.validateToken(request.getTempToken())) {
            throw new ApiException("Temporary token has expired or is invalid. Please sign in again.");
        }

        Claims claims = tokenProvider.getClaimsFromToken(request.getTempToken());
        String userIdStr = claims.getSubject();
        String email = claims.get("email", String.class);
        String companyIdStr = claims.get("companyId", String.class);

        UUID userId = UUID.fromString(userIdStr);
        UUID companyId = companyIdStr != null ? UUID.fromString(companyIdStr) : null;

        if (!otpService.verifyOtp(email, request.getOtp())) {
            throw new ApiException("Invalid or expired OTP code.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setVerified(true);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        // Fetch primary role and company name
        Role role = Role.EMPLOYEE;
        String companyName = "Platform";

        if (companyId != null) {
            CompanyMembership membership = membershipRepository.findByCompanyIdAndUserId(companyId, userId)
                    .orElse(null);
            if (membership != null) {
                role = membership.getRole();
                companyName = membership.getCompany().getName();
            }
        } else {
            // Check if Super Admin
            List<CompanyMembership> memberships = membershipRepository.findByUserId(userId);
            if (!memberships.isEmpty()) {
                role = memberships.get(0).getRole();
                companyId = memberships.get(0).getCompany().getId();
                companyName = memberships.get(0).getCompany().getName();
            }
        }

        String accessToken = tokenProvider.generateAccessToken(userId, email, companyId, role);

        return AuthResponse.builder()
                .otpRequired(false)
                .accessToken(accessToken)
                .tokenType("Bearer")
                .userId(userId)
                .email(email)
                .companyId(companyId)
                .companyName(companyName)
                .role(role)
                .build();
    }
}
