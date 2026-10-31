package com.h_group.gvs_terceirizacoes_desktop.dto;

import java.util.UUID;

public record ClientDTO(
		UUID id,
		String legalName,
		String cnpj,
		String email,
		String phone,
		String address,
		String status
) {
}
