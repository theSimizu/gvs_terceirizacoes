package com.h_group.gvs_terceirizacoes.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ContractStatus {
    ACTIVE("active"),
    CLOSED("closed");

    private final String value;

    ContractStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ContractStatus fromValue(String value) {
        for (ContractStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid contract status: " + value);
    }
}
