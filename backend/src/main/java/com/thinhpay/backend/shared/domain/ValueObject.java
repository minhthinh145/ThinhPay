package com.thinhpay.backend.shared.domain;

import java.io.Serializable;

/**
 * Marker interface for Value Objects in DDD.
 *
 * Characteristics:
 * - Immutable (no setters)
 * - Identified by attributes, not by ID
 * - Equality based on all attributes
 *
 * Examples: Money, Currency, Address, Email
 */
public interface ValueObject<T> extends Serializable {
    /**
     * Value Objects are equal if all attributes are equal
     */
    boolean equals(Object obj);

    /**
     * Value Objects must implement proper hashCode based on all attributes
     */
    int hashCode();
}
