package com.thinhpay.backend.shared.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger Configuration
 * Accessible at: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI thinhPayOpenAPI() {
        // Security scheme for JWT Bearer token
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT authentication token. Format: Bearer <token>");

        // Security requirement to apply globally
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");

        // API Info
        Info info = new Info()
                .title("ThinhPay API")
                .version("1.0.0")
                .description("""
                        ## ThinhPay Digital Payment Platform API
                        
                        **Features:**
                        - 🔐 JWT Authentication with refresh token rotation
                        - 👤 User registration with OTP verification
                        - 🏦 Multi-currency banking (VND, USD, EUR)
                        - 💸 Money transfers with real-time FX rates
                        - 📊 Double-entry bookkeeping ledger
                        
                        **Architecture:**
                        - DDD (Domain-Driven Design)
                        - Hexagonal Architecture
                        - CQRS patterns
                        
                        **Security:**
                        - Password hashing with BCrypt
                        - Token blacklisting on logout
                        - Rate limiting (coming soon)
                        - Session management
                        
                        ---
                        
                        ### How to Use
                        
                        1. **Register**: POST `/api/v1/auth/register`
                        2. **Verify OTP**: POST `/api/v1/auth/verify-otp` (check email)
                        3. **Login**: POST `/api/v1/auth/login` → Get JWT tokens
                        4. **Use Token**: Click 🔓 "Authorize" button, paste: `Bearer <your-access-token>`
                        5. **Access Protected Endpoints**: All `/api/v1/users/**` and `/api/v1/banking/**` endpoints
                        
                        ---
                        
                        📧 Support: support@thinhpay.com
                        """)
                .contact(new Contact()
                        .name("ThinhPay Support")
                        .email("support@thinhpay.com")
                        .url("https://thinhpay.com"));

        // Server configurations
        Server devServer = new Server()
                .url("http://localhost:8080")
                .description("Development Server");

        Server prodServer = new Server()
                .url("https://api.thinhpay.com")
                .description("Production Server");

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", securityScheme))
                .addSecurityItem(securityRequirement);
    }
}
