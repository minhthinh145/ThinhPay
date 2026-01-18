package com.thinhpay.backend.shared.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ThinhPay application configuration properties.
 * Maps properties from application.yaml với prefix "thinhpay".
 */
@Component
@ConfigurationProperties(prefix = "thinhpay")
@Data
public class ThinhPayProperties {

    private App app = new App();
    private Email email = new Email();

    @Data
    public static class App {
        /**
         * Application name
         */
        private String name;

        /**
         * Frontend URL - maps from "frontend-url" in YAML
         */
        private String frontendUrl;
    }

    @Data
    public static class Email {
        /**
         * Email sender address
         */
        private String from;

        /**
         * Email sender name - maps from "from-name" in YAML
         */
        private String fromName;
    }
}
