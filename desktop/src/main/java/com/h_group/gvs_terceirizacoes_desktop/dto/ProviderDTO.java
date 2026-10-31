package com.h_group.gvs_terceirizacoes_desktop.dto;

import java.util.UUID;

public record ProviderDTO(
		UUID id,
		String name,
		String taxId,
		String specialty,
		String email,
		String phone,
		boolean available
) {
}
