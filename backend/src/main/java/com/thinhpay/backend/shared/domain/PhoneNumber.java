package com.thinhpay.backend.shared.domain;

import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;

/**
 * PhoneNumber Value Object
 *
 * Đại diện cho số điện thoại theo chuẩn E.164 (quốc tế)
 * Format: +[country code][subscriber number]
 * Ví dụ: +84901234567, +16505551234
 */
@Embeddable
public class PhoneNumber implements ValueObject<PhoneNumber> {
    // E.164 format: + (optional), 1-15 digits
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\+?[1-9]\\d{1,14}$"
    );

    private String value;

    /**
     * Public no-args constructor for JPA/Hibernate.
     * Do not use directly in application code.
     */
    public PhoneNumber() {
        // Required by JPA
    }

    public PhoneNumber(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }

        String normalized = value.trim();

        if (!PHONE_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid phone number format: " + value);
        }

        this.value = normalized;
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
        PhoneNumber that = (PhoneNumber) obj;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
