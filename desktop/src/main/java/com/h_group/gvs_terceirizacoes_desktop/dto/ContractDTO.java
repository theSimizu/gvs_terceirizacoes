package com.h_group.gvs_terceirizacoes_desktop.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ContractDTO(
		UUID id,
		UUID clientId,
		ClientDTO client,
		UUID providerId,
		ProviderDTO provider,
		LocalDate startDate,
		LocalDate endDate,
		BigDecimal agreedValue,
		String status
) {
}
