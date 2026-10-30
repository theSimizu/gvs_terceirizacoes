package com.h_group.gvs_terceirizacoes.dto.response;

import com.h_group.gvs_terceirizacoes.entity.ServiceOrder;
import com.h_group.gvs_terceirizacoes.entity.ServiceOrderStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ServiceOrderResponse(
        UUID id,
        UUID contractId,
        ContractResponse contract,
        String description,
        LocalDate executionDate,
        ServiceOrderStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public static ServiceOrderResponse from(ServiceOrder serviceOrder) {
        return new ServiceOrderResponse(
                serviceOrder.getId(),
                serviceOrder.getContract().getId(),
                ContractResponse.from(serviceOrder.getContract()),
                serviceOrder.getDescription(),
                serviceOrder.getExecutionDate(),
                serviceOrder.getStatus(),
                serviceOrder.getCreatedAt(),
                serviceOrder.getUpdatedAt()
        );
    }
}
