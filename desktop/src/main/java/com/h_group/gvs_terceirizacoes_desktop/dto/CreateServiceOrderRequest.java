package com.h_group.gvs_terceirizacoes_desktop.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CreateServiceOrderRequest(
		UUID contractId,
		String description,
		LocalDate executionDate,
		String status
) {
}
