package com.h_group.gvs_terceirizacoes.dto.response;

import com.h_group.gvs_terceirizacoes.entity.Client;
import com.h_group.gvs_terceirizacoes.entity.ClientStatus;

import java.time.Instant;
import java.util.UUID;

public record ClientResponse(
        UUID id,
        String legalName,
        String cnpj,
        String email,
        String phone,
        String address,
        ClientStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getLegalName(),
                client.getCnpj(),
                client.getEmail(),
                client.getPhone(),
                client.getAddress(),
                client.getStatus(),
                client.getCreatedAt(),
                client.getUpdatedAt()
        );
    }
}
