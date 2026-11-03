package com.h_group.gvs_terceirizacoes.dto.response;

import com.h_group.gvs_terceirizacoes.entity.User;
import com.h_group.gvs_terceirizacoes.entity.UserRole;

import java.util.UUID;

/**
 * Identificação pública de uma conta, sem senha ou hash.
 * @param id identificador persistido
 * @param name nome da conta
 * @param email e-mail da conta
 * @param role perfil atual
 */
public record UserResponse(UUID id, String name, String email, UserRole role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
