package com.h_group.gvs_terceirizacoes_desktop.dto;

public record CreateProviderRequest(
		String name,
		String taxId,
		String specialty,
		String email,
		String phone,
		Boolean available
) {
}
