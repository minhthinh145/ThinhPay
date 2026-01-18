package com.thinhpay.backend.shared.infrastructure.security;

import com.thinhpay.backend.modules.iam.domain.user.IamUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT Token Provider - Shared Infrastructure.
 * Generate and validate JWT tokens using JJWT library with HS512 algorithm.
 */
@Component
@Slf4j
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenValidity;  // 15 minutes
    private final long refreshTokenValidity; // 7 days

    public JwtTokenProvider(
        @Value("${jwt.secret:thinhpay-secret-key-change-this-in-production-minimum-512-bits}") String secret,
        @Value("${jwt.access-token-validity:900000}") long accessTokenValidity,
        @Value("${jwt.refresh-token-validity:604800000}") long refreshTokenValidity
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
    }

    /**
     * Generate access token with JTI for user.
     * TTL: 15 minutes (default)
     */
    public String generateAccessToken(IamUser user) {
        String jti = UUID.randomUUID().toString();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenValidity);

        return Jwts.builder()
            .subject(user.getId().toString())
            .id(jti) // JWT ID for blacklist
            .claim("email", user.getEmailValue())
            .claim("role", user.getRoleId())
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key, Jwts.SIG.HS512)
            .compact();
    }

    /**
     * Generate access token with custom JTI (for session tracking).
     */
    public String generateAccessToken(UUID userId, String jti) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenValidity);

        return Jwts.builder()
            .subject(userId.toString())
            .id(jti)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key, Jwts.SIG.HS512)
            .compact();
    }

    /**
     * Generate refresh token.
     * TTL: 7 days (default)
     */
    public String generateRefreshToken(UUID userId) {
        String jti = UUID.randomUUID().toString();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenValidity);

        return Jwts.builder()
            .subject(userId.toString())
            .id(jti)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key, Jwts.SIG.HS512)
            .compact();
    }

    /**
     * Validate JWT token.
     * Returns true if token is valid and not expired.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        } catch (io.jsonwebtoken.security.SecurityException e) {
            log.error("JWT security/signature error: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get claims from token.
     */
    public Claims getClaims(String token) {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    /**
     * Get JWT ID (JTI) from token.
     */
    public String getJti(String token) {
        return getClaims(token).getId();
    }

    /**
     * Get user ID from token.
     */
    public UUID getUserId(String token) {
        String subject = getClaims(token).getSubject();
        return UUID.fromString(subject);
    }

    /**
     * Get email from token.
     */
    public String getEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    /**
     * Get role from token.
     */
    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    /**
     * Check if token is expired.
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getClaims(token).getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
