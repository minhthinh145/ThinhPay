package com.thinhpay.backend.shared.infrastructure.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Email Template Loader
 *
 * Loads and processes HTML email templates from classpath.
 * Supports placeholder replacement using {{variableName}} syntax.
 *
 * @author ThinhPay Team
 * @since 2026-01-18
 */
@Component
@Slf4j
public class EmailTemplateLoader {

    private static final String TEMPLATE_BASE_PATH = "email-templates/";

    /**
     * Load email template from classpath resources.
     *
     * @param templateName Name of template file (e.g., "otp-verification.html")
     * @return Template content as string
     * @throws RuntimeException if template not found or cannot be read
     */
    public String loadTemplate(String templateName) {
        try {
            String path = TEMPLATE_BASE_PATH + templateName;
            ClassPathResource resource = new ClassPathResource(path);

            if (!resource.exists()) {
                log.error("Email template not found: {}", path);
                throw new IllegalArgumentException("Template not found: " + templateName);
            }

            try (InputStream inputStream = resource.getInputStream()) {
                String template = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
                log.debug("Loaded email template: {} ({} bytes)", templateName, template.length());
                return template;
            }

        } catch (IOException e) {
            log.error("Failed to load email template: {}", templateName, e);
            throw new RuntimeException("Failed to load email template: " + templateName, e);
        }
    }

    /**
     * Process template by replacing placeholders with actual values.
     *
     * Placeholders use {{variableName}} syntax.
     *
     * @param template Template content with placeholders
     * @param variables Map of variable name to value
     * @return Processed template with values substituted
     */
    public String processTemplate(String template, Map<String, String> variables) {
        if (template == null || template.isEmpty()) {
            log.warn("Empty template provided for processing");
            return template;
        }

        if (variables == null || variables.isEmpty()) {
            log.debug("No variables provided for template processing");
            return template;
        }

        String processed = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            processed = processed.replace(placeholder, value);
        }

        // Log if any placeholders remain unreplaced (potential issue)
        if (processed.contains("{{") && processed.contains("}}")) {
            log.warn("Template still contains unreplaced placeholders after processing");
        }

        log.debug("Processed template with {} variables", variables.size());
        return processed;
    }

    /**
     * Load and process template in one call.
     *
     * @param templateName Name of template file
     * @param variables Map of variable name to value
     * @return Processed template ready to send
     */
    public String loadAndProcess(String templateName, Map<String, String> variables) {
        String template = loadTemplate(templateName);
        return processTemplate(template, variables);
    }
}
