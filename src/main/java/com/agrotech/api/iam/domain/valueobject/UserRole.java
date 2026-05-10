package com.agrotech.api.iam.domain.valueobject;

public enum UserRole {
    ADMIN,
    ADVISOR,
    FARMER;

    public static UserRole from(String value) {
        try {
            return UserRole.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException("Invalid role: " + value);
        }
    }

    public String authority() {
        return "ROLE_" + name();
    }
}
