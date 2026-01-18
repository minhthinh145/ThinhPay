package com.thinhpay.backend.shared.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * Exception khi không tìm thấy resource (User, Account, Transaction...).
 */
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ResourceNotFoundException extends RuntimeException {
    String resourceName;
    String fieldName;
    Object fieldValue;

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
