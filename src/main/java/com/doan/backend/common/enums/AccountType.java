package com.doan.backend.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum AccountType {
    ADMIN("admin", false),
    KHACH("khach", true),
    CUA_HANG("cuaHang", true);

    private final String value;
    private final boolean registerAllowed;

    AccountType(String value, boolean registerAllowed) {
        this.value = value;
        this.registerAllowed = registerAllowed;
    }

    @JsonCreator
    public static AccountType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(type -> type.value.equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("phanQuyen chi chap nhan khach hoac cuaHang"));
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public boolean isRegisterAllowed() {
        return registerAllowed;
    }
}
