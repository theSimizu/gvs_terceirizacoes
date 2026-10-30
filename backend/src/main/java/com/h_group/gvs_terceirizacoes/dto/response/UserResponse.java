package com.h_group.gvs_terceirizacoes.dto.response;

import com.h_group.gvs_terceirizacoes.entity.User;

import java.util.UUID;

public record UserResponse(UUID id, String name, String email) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
