package com.h_group.gvs_terceirizacoes.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3) String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String password
) {
}
