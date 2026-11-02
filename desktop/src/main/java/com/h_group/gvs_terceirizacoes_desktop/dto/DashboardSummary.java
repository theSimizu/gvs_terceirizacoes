package com.h_group.gvs_terceirizacoes_desktop.dto;

import java.util.Map;

public record DashboardSummary(
		long clientsCount,
		long availableProvidersCount,
		long activeContractsCount,
		Map<String, Long> ordersByStatus,
		Map<String, Long> providersBySpecialty
) {
}
