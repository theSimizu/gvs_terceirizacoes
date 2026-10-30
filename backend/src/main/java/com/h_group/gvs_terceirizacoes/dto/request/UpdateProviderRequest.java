package com.h_group.gvs_terceirizacoes.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateProviderRequest(
        @Size(min = 3) String name,
        @Size(min = 11, max = 18) String taxId,
        String specialty,
        @Email String email,
        @Size(min = 10, max = 20) String phone,
        Boolean available
) {
}
