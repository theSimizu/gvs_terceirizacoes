package com.h_group.gvs_terceirizacoes_desktop.dto;

public record UserDTO(String id, String name, String email, String role) {

	public boolean isAdministrator() {
		return "adm".equals(role);
	}
}
