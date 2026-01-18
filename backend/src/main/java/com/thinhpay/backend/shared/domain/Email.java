package com.thinhpay.backend.shared.domain;

import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;

/**
 * Email Value Object
 *
 * Đại diện cho email address với validation
 */
@Embeddable
public class Email implements ValueObject<Email> {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private String value;

    /**
     * Public no-args constructor for JPA/Hibernate.
     * Do not use directly in application code.
     */
    public Email() {
        // Required by JPA
    }

    public Email(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }

        this.value = value.toLowerCase().trim();
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Email email = (Email) obj;
        return value.equals(email.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
