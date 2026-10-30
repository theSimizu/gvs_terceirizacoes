package com.h_group.gvs_terceirizacoes.dto.request;

import com.h_group.gvs_terceirizacoes.entity.ServiceOrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateServiceOrderRequest(
        @NotNull UUID contractId,
        @NotBlank String description,
        @NotNull LocalDate executionDate,
        ServiceOrderStatus status
) {
}
