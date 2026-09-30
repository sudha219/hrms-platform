package com.hrms.security;

import com.hrms.enums.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long jwtExpirationMs;
    private final long otpExpirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms:86400000}") long jwtExpirationMs,
            @Value("${app.jwt.otp-expiration-ms:300000}") long otpExpirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.jwtExpirationMs = jwtExpirationMs;
        this.otpExpirationMs = otpExpirationMs;
    }

    public String generateAccessToken(UUID userId, String email, UUID companyId, Role role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        JwtBuilder builder = Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey);

        if (companyId != null) {
            builder.claim("companyId", companyId.toString());
        }

        return builder.compact();
    }

    public String generateOtpTemporaryToken(UUID userId, String email, UUID companyId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + otpExpirationMs);

        JwtBuilder builder = Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("type", "OTP_PENDING")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey);

        if (companyId != null) {
            builder.claim("companyId", companyId.toString());
        }

        return builder.compact();
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }
}
