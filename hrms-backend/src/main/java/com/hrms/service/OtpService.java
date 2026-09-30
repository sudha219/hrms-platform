package com.hrms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class OtpService {

    private final SecureRandom random = new SecureRandom();
    // In-memory OTP storage (or Redis in production)
    private final Map<String, OtpEntry> otpStorage = new ConcurrentHashMap<>();

    private record OtpEntry(String code, Instant expiresAt) {}

    public String generateAndSendOtp(String email) {
        int otpValue = 100000 + random.nextInt(900000);
        String otp = String.valueOf(otpValue);
        Instant expiresAt = Instant.now().plusSeconds(300); // 5 minutes

        otpStorage.put(email, new OtpEntry(otp, expiresAt));
        log.info("[MOCK EMAIL DELIVERY] Sent OTP code {} to user email: {}", otp, email);
        return otp;
    }

    public boolean verifyOtp(String email, String inputOtp) {
        OtpEntry entry = otpStorage.get(email);
        if (entry == null) {
            return false;
        }

        if (Instant.now().isAfter(entry.expiresAt())) {
            otpStorage.remove(email);
            return false;
        }

        boolean isValid = entry.code().equals(inputOtp);
        if (isValid) {
            otpStorage.remove(email); // single-use
        }
        return isValid;
    }
}
