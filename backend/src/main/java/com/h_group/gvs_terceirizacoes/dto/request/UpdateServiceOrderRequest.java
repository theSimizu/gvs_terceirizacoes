package com.h_group.gvs_terceirizacoes.dto.request;

import com.h_group.gvs_terceirizacoes.entity.ServiceOrderStatus;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateServiceOrderRequest(
        UUID contractId,
        String description,
        LocalDate executionDate,
        ServiceOrderStatus status
) {
}
