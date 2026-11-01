package com.h_group.gvs_terceirizacoes_desktop.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateContractRequest(
		UUID clientId,
		UUID providerId,
		LocalDate startDate,
		LocalDate endDate,
		BigDecimal agreedValue,
		String status
) {
}
