package com.h_group.gvs_terceirizacoes_desktop.dto;

public record CreateClientRequest(
		String legalName,
		String cnpj,
		String email,
		String phone,
		String address,
		String status
) {
}
