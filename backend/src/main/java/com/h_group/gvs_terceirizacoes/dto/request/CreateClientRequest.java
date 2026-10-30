package com.h_group.gvs_terceirizacoes.dto.request;

import com.h_group.gvs_terceirizacoes.entity.ClientStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateClientRequest(
        @NotBlank @Size(min = 3) String legalName,
        @NotBlank @Size(min = 14, max = 18) String cnpj,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 10, max = 20) String phone,
        String address,
        ClientStatus status
) {
}
