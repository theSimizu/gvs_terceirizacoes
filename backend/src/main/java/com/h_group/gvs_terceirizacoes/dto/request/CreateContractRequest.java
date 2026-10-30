package com.h_group.gvs_terceirizacoes.dto.request;

import com.h_group.gvs_terceirizacoes.entity.ContractStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateContractRequest(
        @NotNull UUID clientId,
        @NotNull UUID providerId,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        @DecimalMin("0.00") BigDecimal agreedValue,
        ContractStatus status
) {
}
