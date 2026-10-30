package com.h_group.gvs_terceirizacoes.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ClientStatus {
    ACTIVE("active"),
    INACTIVE("inactive");

    private final String value;

    ClientStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ClientStatus fromValue(String value) {
        for (ClientStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid client status: " + value);
    }
}
