package com.h_group.gvs_terceirizacoes_desktop.dto;

public record UpdateProviderRequest(
		String name,
		String taxId,
		String specialty,
		String email,
		String phone,
		Boolean available
) {
}
