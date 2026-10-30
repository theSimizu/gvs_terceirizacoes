package com.h_group.gvs_terceirizacoes.dto.request;

import com.h_group.gvs_terceirizacoes.entity.ClientStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateClientRequest(
        @Size(min = 3) String legalName,
        @Size(min = 14, max = 18) String cnpj,
        @Email String email,
        @Size(min = 10, max = 20) String phone,
        String address,
        ClientStatus status
) {
}
