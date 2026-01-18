package com.thinhpay.backend.shared.infrastructure.security;

import com.thinhpay.backend.modules.iam.application.port.out.TokenBlacklistRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

/**
 * JWT Authentication Filter - Shared Infrastructure.
 * - Extract JWT from Authorization header
 * - Validate token
 * - Check blacklist
 * - Set SecurityContext for authenticated requests
 */
@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    JwtTokenProvider jwtTokenProvider;
    TokenBlacklistRepository tokenBlacklistRepository;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 1. Extract JWT from request
            String jwt = extractJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                // 2. Validate token
                if (jwtTokenProvider.validateToken(jwt)) {

                    // 3. Check blacklist
                    String jti = jwtTokenProvider.getJti(jwt);
                    if (tokenBlacklistRepository.existsByJti(jti)) {
                        log.warn("Rejected blacklisted token: jti={}", jti);
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has been revoked");
                        return;
                    }

                    // 4. Extract user info from token
                    UUID userId = jwtTokenProvider.getUserId(jwt);
                    String role = jwtTokenProvider.getRole(jwt);

                    // 5. Create authentication object
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            Collections.singletonList(authority)
                        );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 6. Set SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("Authenticated user: {} with role: {}", userId, role);
                } else {
                    log.warn("Invalid JWT token");
                }
            }
        } catch (Exception e) {
            log.error("Could not set user authentication", e);
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT from Authorization header.
     * Format: "Bearer <token>"
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    /**
     * Skip JWT authentication for public endpoints.
     * Let SecurityConfig handle authorization for these paths.
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();

        // Skip Swagger/OpenAPI endpoints
        if (path.startsWith("/swagger-ui") ||
            path.startsWith("/v3/api-docs") ||
            path.startsWith("/swagger-resources") ||
            path.startsWith("/webjars")) {
            return true;
        }

        // Skip public auth endpoints
        if (path.startsWith("/api/v1/auth")) {
            return true;
        }

        // Skip actuator endpoints
        if (path.startsWith("/actuator")) {
            return true;
        }

        return false;
    }
}
