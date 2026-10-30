package com.h_group.gvs_terceirizacoes.dto.response;

import com.h_group.gvs_terceirizacoes.entity.Contract;
import com.h_group.gvs_terceirizacoes.entity.ContractStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ContractResponse(
        UUID id,
        UUID clientId,
        ClientResponse client,
        UUID providerId,
        ProviderResponse provider,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal agreedValue,
        ContractStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public static ContractResponse from(Contract contract) {
        return new ContractResponse(
                contract.getId(),
                contract.getClient().getId(),
                ClientResponse.from(contract.getClient()),
                contract.getProvider().getId(),
                ProviderResponse.from(contract.getProvider()),
                contract.getStartDate(),
                contract.getEndDate(),
                contract.getAgreedValue(),
                contract.getStatus(),
                contract.getCreatedAt(),
                contract.getUpdatedAt()
        );
    }
}
