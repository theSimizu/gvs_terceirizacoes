package com.h_group.gvs_terceirizacoes.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Perfis reconhecidos pela aplicação: administrador e usuário.
 * O banco persiste os nomes do enum; o JSON usa os valores em letras minúsculas.
 */
public enum UserRole {
    ADM("adm"), USER("user");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static UserRole fromValue(String value) {
        for (UserRole role : values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid user role");
    }
}
