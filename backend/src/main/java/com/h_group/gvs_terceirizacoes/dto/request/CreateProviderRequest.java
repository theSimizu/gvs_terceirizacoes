package com.h_group.gvs_terceirizacoes.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProviderRequest(
        @NotBlank @Size(min = 3) String name,
        @NotBlank @Size(min = 11, max = 18) String taxId,
        @NotBlank String specialty,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 10, max = 20) String phone,
        Boolean available
) {
}
